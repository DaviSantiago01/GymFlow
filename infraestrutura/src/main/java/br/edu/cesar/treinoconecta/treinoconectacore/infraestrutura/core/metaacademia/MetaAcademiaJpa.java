package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.metaacademia;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.MetaAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.SituacaoMetaAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.IndicadorAcademia;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity @Table(name="MAC_META_ACADEMIA")
public class MetaAcademiaJpa implements IAgregadoJpa<MetaAcademia> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="MAC_ID")
    private @Nullable Long identificador;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="MAC_INDICADOR") private IndicadorAcademia indicador;
    @NotNull @Column(name="MAC_VALOR_DESEJADO") private BigDecimal valorDesejado;
    @NotNull @Column(name="MAC_INICIO") private LocalDate inicio;
    @NotNull @Column(name="MAC_FIM") private LocalDate fim;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="MAC_SITUACAO") private SituacaoMetaAcademia situacao;
    @NotNull @Column(name="MAC_CRIACAO") private Instant criacao;
    @NotNull @Column(name="MAC_RESPONSAVEL") private String responsavel;

    protected MetaAcademiaJpa() { }

    public MetaAcademiaJpa(@Nullable Long identificador, IndicadorAcademia indicador, BigDecimal valorDesejado,
            LocalDate inicio, LocalDate fim, SituacaoMetaAcademia situacao, Instant criacao, String responsavel) {
        this.identificador = identificador;
        this.indicador = indicador;
        this.valorDesejado = valorDesejado;
        this.inicio = inicio;
        this.fim = fim;
        this.situacao = situacao;
        this.criacao = criacao;
        this.responsavel = responsavel;
    }

    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public IndicadorAcademia getIndicador() { return indicador; }
    public BigDecimal getValorDesejado() { return valorDesejado; }
    public LocalDate getInicio() { return inicio; }
    public LocalDate getFim() { return fim; }
    public SituacaoMetaAcademia getSituacao() { return situacao; }
    public Instant getCriacao() { return criacao; }
    public String getResponsavel() { return responsavel; }
}
