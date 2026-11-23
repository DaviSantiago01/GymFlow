package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

/**
 * Cobrança de um período de uma adesão. A situação gravada é ABERTA ou PAGA; o atraso é calculado pelo vencimento, e o
 * valor devido inclui multa e juros proporcionais aos dias de atraso.
 */
public final class Mensalidade implements IAgregado<MensalidadeIdentificador> {
    private static final BigDecimal CEM = new BigDecimal("100");

    private final Optional<MensalidadeIdentificador> identificador;
    private final AdesaoPlanoIdentificador adesao;
    private final AlunoIdentificador aluno;
    private final PeriodoCobranca periodo;
    private final LocalDate vencimento;
    private final BigDecimal valor;
    private final SituacaoMensalidade situacao;
    private final List<Pagamento> pagamentos;

    public Mensalidade(Optional<MensalidadeIdentificador> identificador, AdesaoPlanoIdentificador adesao,
            AlunoIdentificador aluno, PeriodoCobranca periodo, LocalDate vencimento, BigDecimal valor,
            SituacaoMensalidade situacao, List<Pagamento> pagamentos) {
        this.identificador = Objects.requireNonNull(identificador);
        this.adesao = Objects.requireNonNull(adesao);
        this.aluno = Objects.requireNonNull(aluno);
        this.periodo = Objects.requireNonNull(periodo);
        this.vencimento = Objects.requireNonNull(vencimento);
        Objects.requireNonNull(valor);
        Validate.isTrue(valor.signum() > 0, "O valor da mensalidade deve ser maior que zero");
        this.valor = valor.setScale(2, RoundingMode.HALF_UP);
        this.situacao = Objects.requireNonNull(situacao);
        this.pagamentos = List.copyOf(pagamentos);
        Validate.isTrue(situacao != SituacaoMensalidade.ATRASADA, "O atraso é calculado pelo vencimento e não é gravado");
        Validate.isTrue(!vencimento.isBefore(periodo.inicio()), "O vencimento não pode anteceder o período");
        long confirmados = this.pagamentos.stream().filter(Pagamento::confirmado).count();
        Validate.isTrue(confirmados <= 1, "A mensalidade possui mais de um pagamento confirmado");
        Validate.isTrue((situacao == SituacaoMensalidade.PAGA) == (confirmados == 1), "Situação da mensalidade inconsistente");
    }

    public static Mensalidade gerar(AdesaoPlano adesao, PeriodoCobranca periodo, BigDecimal valor) {
        Objects.requireNonNull(adesao);
        Objects.requireNonNull(periodo);
        return new Mensalidade(Optional.empty(), adesao.getIdentificador().orElseThrow(), adesao.getAluno(), periodo,
                vencimentoDo(periodo), valor, SituacaoMensalidade.ABERTA, List.of());
    }

    /** O vencimento é contado a partir do início do período de cobrança. */
    public static LocalDate vencimentoDo(PeriodoCobranca periodo) {
        return Objects.requireNonNull(periodo).inicio().plusDays(ParametrosFinanceiros.PRAZO_PAGAMENTO_DIAS);
    }

    public SituacaoMensalidade situacaoEm(LocalDate hoje) {
        Objects.requireNonNull(hoje);
        if (situacao == SituacaoMensalidade.ABERTA && hoje.isAfter(vencimento)) {
            return SituacaoMensalidade.ATRASADA;
        }
        return situacao;
    }

    public boolean atrasadaEm(LocalDate hoje) {
        return situacaoEm(hoje) == SituacaoMensalidade.ATRASADA;
    }

    public long diasDeAtraso(LocalDate hoje) {
        return atrasadaEm(hoje) ? ChronoUnit.DAYS.between(vencimento, hoje) : 0;
    }

    public BigDecimal multaEm(LocalDate hoje) {
        if (!atrasadaEm(hoje)) {
            return BigDecimal.ZERO.setScale(2);
        }
        return valor.multiply(ParametrosFinanceiros.PERCENTUAL_MULTA).divide(CEM, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal jurosEm(LocalDate hoje) {
        var dias = BigDecimal.valueOf(diasDeAtraso(hoje));
        return valor.multiply(ParametrosFinanceiros.PERCENTUAL_JUROS_MENSAL).multiply(dias)
                .divide(CEM.multiply(BigDecimal.valueOf(ParametrosFinanceiros.DIAS_DO_MES_PARA_JUROS)), 2, RoundingMode.HALF_UP);
    }

    /** Valor a pagar na data: o valor do plano, acrescido de multa e juros quando a mensalidade está atrasada. */
    public BigDecimal valorDevidoEm(LocalDate hoje) {
        return valor.add(multaEm(hoje)).add(jurosEm(hoje));
    }

    public Mensalidade registrarPagamento(FormaPagamento forma, Instant agora, LocalDate hoje) {
        Objects.requireNonNull(forma);
        Objects.requireNonNull(agora);
        Validate.validState(situacao == SituacaoMensalidade.ABERTA, MensalidadeServico.MENSALIDADE_NAO_ABERTA);
        var novos = new ArrayList<>(pagamentos);
        novos.add(Pagamento.confirmar(valorDevidoEm(hoje), forma, agora));
        return new Mensalidade(identificador, adesao, aluno, periodo, vencimento, valor, SituacaoMensalidade.PAGA, novos);
    }

    public Mensalidade estornarPagamento(long pagamento, String motivo, Instant agora) {
        Objects.requireNonNull(agora);
        var novos = new ArrayList<Pagamento>();
        boolean encontrado = false;
        for (var existente : pagamentos) {
            if (existente.getIdentificador().equals(Optional.of(pagamento))) {
                novos.add(existente.estornar(motivo, agora));
                encontrado = true;
            } else {
                novos.add(existente);
            }
        }
        Validate.validState(encontrado, MensalidadeServico.PAGAMENTO_INEXISTENTE);
        return new Mensalidade(identificador, adesao, aluno, periodo, vencimento, valor, SituacaoMensalidade.ABERTA, novos);
    }

    public Optional<MensalidadeIdentificador> getIdentificador() { return identificador; }
    public AdesaoPlanoIdentificador getAdesao() { return adesao; }
    public AlunoIdentificador getAluno() { return aluno; }
    public PeriodoCobranca getPeriodo() { return periodo; }
    public LocalDate getVencimento() { return vencimento; }
    public BigDecimal getValor() { return valor; }
    public SituacaoMensalidade getSituacao() { return situacao; }
    public List<Pagamento> getPagamentos() { return pagamentos; }
}
