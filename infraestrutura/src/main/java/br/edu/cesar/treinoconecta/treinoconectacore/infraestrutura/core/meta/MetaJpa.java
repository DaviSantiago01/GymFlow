package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.meta;

import java.math.BigDecimal;
import java.time.LocalDate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.Meta;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.SituacaoMeta;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaofisica.AvaliacaoFisicaJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.PersonalJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

@Entity @Table(name="MET_META")
public class MetaJpa implements IAgregadoJpa<Meta> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="MET_ID")
    private @Nullable Long identificador;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ALU_ID") private AlunoJpa aluno;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="PER_ID") private PersonalJpa personal;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="MET_INDICADOR") private IndicadorFisico indicador;
    @NotNull @Column(name="MET_VALOR_INICIAL") private BigDecimal valorInicial;
    @NotNull @Column(name="MET_VALOR_DESEJADO") private BigDecimal valorDesejado;
    @NotNull @Column(name="MET_INICIO") private LocalDate inicio;
    @NotNull @Column(name="MET_FIM") private LocalDate fim;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="AVF_ID") private AvaliacaoFisicaJpa avaliacaoReferencia;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="MET_SITUACAO") private SituacaoMeta situacao;
    @Column(name="MET_DATA_ATINGIDA") private @Nullable LocalDate dataAtingida;

    protected MetaJpa() { }

    public MetaJpa(@Nullable Long identificador, AlunoJpa aluno, PersonalJpa personal, IndicadorFisico indicador,
            BigDecimal valorInicial, BigDecimal valorDesejado, LocalDate inicio, LocalDate fim,
            AvaliacaoFisicaJpa avaliacaoReferencia, SituacaoMeta situacao, @Nullable LocalDate dataAtingida) {
        this.identificador = identificador;
        this.aluno = aluno;
        this.personal = personal;
        this.indicador = indicador;
        this.valorInicial = valorInicial;
        this.valorDesejado = valorDesejado;
        this.inicio = inicio;
        this.fim = fim;
        this.avaliacaoReferencia = avaliacaoReferencia;
        this.situacao = situacao;
        this.dataAtingida = dataAtingida;
    }

    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public AlunoJpa getAluno() { return aluno; }
    public PersonalJpa getPersonal() { return personal; }
    public IndicadorFisico getIndicador() { return indicador; }
    public BigDecimal getValorInicial() { return valorInicial; }
    public BigDecimal getValorDesejado() { return valorDesejado; }
    public LocalDate getInicio() { return inicio; }
    public LocalDate getFim() { return fim; }
    public AvaliacaoFisicaJpa getAvaliacaoReferencia() { return avaliacaoReferencia; }
    public SituacaoMeta getSituacao() { return situacao; }
    public @Nullable LocalDate getDataAtingida() { return dataAtingida; }
}
