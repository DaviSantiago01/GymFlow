package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.PeriodoCobranca;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.Plano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

/**
 * Vínculo do aluno com um plano. A periodicidade é a duração do plano no momento da adesão, para que uma alteração
 * posterior do plano não mude os períodos de cobrança já iniciados. O período coberto é [início, encerramento).
 *
 * <p>A recorrência é opcional: sem ela, a cobrança continua até a troca ou o cancelamento; com ela, a adesão prevê uma
 * quantidade fixa de cobranças e termina quando o último período acaba.</p>
 */
public final class AdesaoPlano implements IAgregado<AdesaoPlanoIdentificador> {
    public static final int RECORRENCIA_MINIMA = 1;
    public static final int RECORRENCIA_MAXIMA = 60;

    private final Optional<AdesaoPlanoIdentificador> identificador;
    private final AlunoIdentificador aluno;
    private final PlanoIdentificador plano;
    private final int periodicidadeMeses;
    private final LocalDate inicio;
    private final Optional<LocalDate> encerramento;
    private final SituacaoAdesao situacao;
    private final Optional<Integer> quantidadePeriodos;

    public AdesaoPlano(Optional<AdesaoPlanoIdentificador> identificador, AlunoIdentificador aluno, PlanoIdentificador plano,
            int periodicidadeMeses, LocalDate inicio, Optional<LocalDate> encerramento, SituacaoAdesao situacao) {
        this(identificador, aluno, plano, periodicidadeMeses, inicio, encerramento, situacao, Optional.empty());
    }

    public AdesaoPlano(Optional<AdesaoPlanoIdentificador> identificador, AlunoIdentificador aluno, PlanoIdentificador plano,
            int periodicidadeMeses, LocalDate inicio, Optional<LocalDate> encerramento, SituacaoAdesao situacao,
            Optional<Integer> quantidadePeriodos) {
        this.identificador = Objects.requireNonNull(identificador);
        this.aluno = Objects.requireNonNull(aluno);
        this.plano = Objects.requireNonNull(plano);
        Validate.isTrue(periodicidadeMeses >= Plano.DURACAO_MINIMA_MESES && periodicidadeMeses <= Plano.DURACAO_MAXIMA_MESES,
                "Periodicidade de cobrança inválida");
        this.periodicidadeMeses = periodicidadeMeses;
        this.inicio = Objects.requireNonNull(inicio);
        this.encerramento = Objects.requireNonNull(encerramento);
        this.situacao = Objects.requireNonNull(situacao);
        this.quantidadePeriodos = Objects.requireNonNull(quantidadePeriodos);
        quantidadePeriodos.ifPresent(quantidade -> Validate.isTrue(
                quantidade >= RECORRENCIA_MINIMA && quantidade <= RECORRENCIA_MAXIMA, AdesaoPlanoServico.RECORRENCIA_INVALIDA));
        Validate.isTrue((situacao == SituacaoAdesao.ATIVA) == encerramento.isEmpty(), "Encerramento da adesão inconsistente");
        encerramento.ifPresent(data -> Validate.isTrue(!data.isBefore(inicio), "O encerramento não pode anteceder o início"));
        Validate.isTrue(situacao != SituacaoAdesao.CONCLUIDA || quantidadePeriodos.isPresent(),
                "Somente uma adesão com recorrência pode ser concluída");
    }

    public static AdesaoPlano aderir(AlunoIdentificador aluno, Plano plano, LocalDate hoje) {
        return aderir(aluno, plano, hoje, Optional.empty());
    }

    /** A recorrência informa quantas cobranças a adesão terá; vazia, a cobrança é contínua. */
    public static AdesaoPlano aderir(AlunoIdentificador aluno, Plano plano, LocalDate hoje,
            Optional<Integer> quantidadePeriodos) {
        Objects.requireNonNull(plano);
        Validate.validState(plano.ativo(), AdesaoPlanoServico.PLANO_INDISPONIVEL);
        return new AdesaoPlano(Optional.empty(), aluno, plano.getIdentificador().orElseThrow(), plano.getDuracaoMeses(),
                hoje, Optional.empty(), SituacaoAdesao.ATIVA, quantidadePeriodos);
    }

    /** Na troca, a adesão atual termina no mesmo dia em que a nova começa: os períodos não se sobrepõem. */
    public AdesaoPlano encerrarPorTroca(LocalDate hoje) {
        return finalizar(SituacaoAdesao.ENCERRADA, hoje);
    }

    /** O cancelamento só muda a situação: a adesão e as mensalidades geradas permanecem no histórico. */
    public AdesaoPlano cancelar(LocalDate hoje) {
        return finalizar(SituacaoAdesao.CANCELADA, hoje);
    }

    /** Ao fim do último período contratado, a adesão é concluída com encerramento no dia seguinte a esse período. */
    public AdesaoPlano concluir(LocalDate hoje) {
        Objects.requireNonNull(hoje);
        Validate.validState(ativa(), AdesaoPlanoServico.ADESAO_NAO_ATIVA);
        Validate.validState(prazoCumpridoEm(hoje), AdesaoPlanoServico.PRAZO_NAO_CUMPRIDO);
        return new AdesaoPlano(identificador, aluno, plano, periodicidadeMeses, inicio, Optional.of(fimDaRecorrencia()),
                SituacaoAdesao.CONCLUIDA, quantidadePeriodos);
    }

    private AdesaoPlano finalizar(SituacaoAdesao destino, LocalDate hoje) {
        Objects.requireNonNull(hoje);
        Validate.validState(ativa(), AdesaoPlanoServico.ADESAO_NAO_ATIVA);
        var data = hoje.isBefore(inicio) ? inicio : hoje;
        return new AdesaoPlano(identificador, aluno, plano, periodicidadeMeses, inicio, Optional.of(data), destino,
                quantidadePeriodos);
    }

    public boolean ativa() {
        return situacao == SituacaoAdesao.ATIVA;
    }

    public boolean possuiRecorrencia() {
        return quantidadePeriodos.isPresent();
    }

    /** Verdadeiro quando a adesão tem recorrência e o último período contratado já terminou na data. */
    public boolean prazoCumpridoEm(LocalDate hoje) {
        Objects.requireNonNull(hoje);
        return possuiRecorrencia() && !hoje.isBefore(fimDaRecorrencia());
    }

    /** Último dia coberto pela recorrência, quando houver. */
    public Optional<LocalDate> terminoPrevisto() {
        return quantidadePeriodos.map(quantidade -> periodo(quantidade - 1).fim());
    }

    private LocalDate fimDaRecorrencia() {
        return inicio.plusMonths((long) quantidadePeriodos.orElseThrow() * periodicidadeMeses);
    }

    public PeriodoCobranca periodo(int indice) {
        Validate.isTrue(indice >= 0, "Índice do período inválido");
        var inicioPeriodo = inicio.plusMonths((long) indice * periodicidadeMeses);
        var fimPeriodo = inicio.plusMonths((long) (indice + 1) * periodicidadeMeses).minusDays(1);
        return new PeriodoCobranca(inicioPeriodo, fimPeriodo);
    }

    /** Todos os períodos contratados pela recorrência; vazio quando a cobrança é contínua. */
    public List<PeriodoCobranca> periodosContratados() {
        var periodos = new ArrayList<PeriodoCobranca>();
        for (int indice = 0; indice < quantidadePeriodos.orElse(0); indice++) {
            periodos.add(periodo(indice));
        }
        return periodos;
    }

    /**
     * Períodos de cobrança já iniciados até a data informada, limitados ao encerramento da adesão e, quando houver, à
     * quantidade de cobranças contratadas.
     */
    public List<PeriodoCobranca> periodosIniciadosAte(LocalDate data) {
        Objects.requireNonNull(data);
        var periodos = new ArrayList<PeriodoCobranca>();
        for (int indice = 0;; indice++) {
            if (quantidadePeriodos.isPresent() && indice >= quantidadePeriodos.get()) {
                return periodos;
            }
            var periodo = periodo(indice);
            boolean depoisDoEncerramento = encerramento.map(fim -> !periodo.inicio().isBefore(fim)).orElse(false);
            if (periodo.inicio().isAfter(data) || (indice > 0 && depoisDoEncerramento)) {
                return periodos;
            }
            periodos.add(periodo);
        }
    }

    public Optional<AdesaoPlanoIdentificador> getIdentificador() { return identificador; }
    public AlunoIdentificador getAluno() { return aluno; }
    public PlanoIdentificador getPlano() { return plano; }
    public int getPeriodicidadeMeses() { return periodicidadeMeses; }
    public LocalDate getInicio() { return inicio; }
    public Optional<LocalDate> getEncerramento() { return encerramento; }
    public SituacaoAdesao getSituacao() { return situacao; }
    public Optional<Integer> getQuantidadePeriodos() { return quantidadePeriodos; }
}
