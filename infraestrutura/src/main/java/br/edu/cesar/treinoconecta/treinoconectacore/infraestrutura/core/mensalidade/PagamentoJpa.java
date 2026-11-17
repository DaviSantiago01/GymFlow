package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.mensalidade;

import java.math.BigDecimal;
import java.time.Instant;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.FormaPagamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.ParametrosFinanceiros;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.SituacaoPagamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

@Entity @Table(name="PAG_PAGAMENTO")
public class PagamentoJpa {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="PAG_ID")
    private @Nullable Long identificador;
    @NotNull @Column(name="PAG_VALOR") private BigDecimal valor;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="PAG_FORMA") private FormaPagamento forma;
    @NotNull @Column(name="PAG_REGISTRO") private Instant registro;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="PAG_SITUACAO") private SituacaoPagamento situacao;
    @Column(name="PAG_ESTORNO") private @Nullable Instant estorno;
    @Size(max=ParametrosFinanceiros.TAMANHO_MOTIVO_ESTORNO) @Column(name="PAG_MOTIVO_ESTORNO")
    private @Nullable String motivoEstorno;

    protected PagamentoJpa() { }

    public PagamentoJpa(@Nullable Long identificador, BigDecimal valor, FormaPagamento forma, Instant registro,
            SituacaoPagamento situacao, @Nullable Instant estorno, @Nullable String motivoEstorno) {
        this.identificador = identificador;
        this.valor = valor;
        this.forma = forma;
        this.registro = registro;
        this.situacao = situacao;
        this.estorno = estorno;
        this.motivoEstorno = motivoEstorno;
    }

    public @Nullable Long getIdentificador() { return identificador; }
    public BigDecimal getValor() { return valor; }
    public FormaPagamento getForma() { return forma; }
    public Instant getRegistro() { return registro; }
    public SituacaoPagamento getSituacao() { return situacao; }
    public @Nullable Instant getEstorno() { return estorno; }
    public @Nullable String getMotivoEstorno() { return motivoEstorno; }
}
