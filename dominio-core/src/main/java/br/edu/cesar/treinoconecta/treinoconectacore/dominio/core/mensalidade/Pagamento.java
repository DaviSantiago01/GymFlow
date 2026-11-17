package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;

/** Entidade da mensalidade. Um estorno não apaga o pagamento: registra quando e por que ele foi desfeito. */
public final class Pagamento {
    private final Optional<Long> identificador;
    private final BigDecimal valor;
    private final FormaPagamento forma;
    private final Instant registro;
    private final SituacaoPagamento situacao;
    private final Optional<Instant> estorno;
    private final Optional<String> motivoEstorno;

    public Pagamento(Optional<Long> identificador, BigDecimal valor, FormaPagamento forma, Instant registro,
            SituacaoPagamento situacao, Optional<Instant> estorno, Optional<String> motivoEstorno) {
        this.identificador = Objects.requireNonNull(identificador);
        Objects.requireNonNull(valor);
        Validate.isTrue(valor.signum() > 0, "O valor do pagamento deve ser maior que zero");
        this.valor = valor.setScale(2, RoundingMode.HALF_UP);
        this.forma = Objects.requireNonNull(forma);
        this.registro = Objects.requireNonNull(registro);
        this.situacao = Objects.requireNonNull(situacao);
        this.estorno = Objects.requireNonNull(estorno);
        this.motivoEstorno = Objects.requireNonNull(motivoEstorno);
        boolean estornado = situacao == SituacaoPagamento.ESTORNADO;
        Validate.isTrue(estornado == estorno.isPresent() && estornado == motivoEstorno.isPresent(),
                "Estorno do pagamento inconsistente");
        estorno.ifPresent(data -> Validate.isTrue(!data.isBefore(registro), "O estorno não pode anteceder o pagamento"));
    }

    public static Pagamento confirmar(BigDecimal valor, FormaPagamento forma, Instant agora) {
        return new Pagamento(Optional.empty(), valor, forma, agora, SituacaoPagamento.CONFIRMADO, Optional.empty(),
                Optional.empty());
    }

    public Pagamento estornar(String motivo, Instant agora) {
        Validate.validState(confirmado(), MensalidadeServico.PAGAMENTO_JA_ESTORNADO);
        Validate.notBlank(motivo, MensalidadeServico.MOTIVO_OBRIGATORIO);
        var normalizado = motivo.strip();
        Validate.isTrue(normalizado.length() <= ParametrosFinanceiros.TAMANHO_MOTIVO_ESTORNO, MensalidadeServico.MOTIVO_OBRIGATORIO);
        return new Pagamento(identificador, valor, forma, registro, SituacaoPagamento.ESTORNADO, Optional.of(agora),
                Optional.of(normalizado));
    }

    public boolean confirmado() {
        return situacao == SituacaoPagamento.CONFIRMADO;
    }

    public Optional<Long> getIdentificador() { return identificador; }
    public BigDecimal getValor() { return valor; }
    public FormaPagamento getForma() { return forma; }
    public Instant getRegistro() { return registro; }
    public SituacaoPagamento getSituacao() { return situacao; }
    public Optional<Instant> getEstorno() { return estorno; }
    public Optional<String> getMotivoEstorno() { return motivoEstorno; }
}
