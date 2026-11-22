package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.recomendacao;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.CategoriaRecomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.CriterioRecomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.Recomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.SituacaoRecomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio.ExercicioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.PersonalJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.planodetreino.PlanoDeTreinoJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity @Table(name="REC_RECOMENDACAO")
public class RecomendacaoJpa implements IAgregadoJpa<Recomendacao> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="REC_ID")
    private @Nullable Long identificador;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ALU_ID") private AlunoJpa aluno;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="REC_CATEGORIA") private CategoriaRecomendacao categoria;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="REC_CRITERIO") private CriterioRecomendacao criterio;
    @NotNull @Column(name="REC_ITEM") private String item;
    @NotNull @Column(name="REC_TITULO") private String titulo;
    @NotNull @Column(name="REC_DESCRICAO") private String descricao;
    @NotNull @Column(name="REC_JUSTIFICATIVA") private String justificativa;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="PER_ID") private @Nullable PersonalJpa personal;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="PLT_ID") private @Nullable PlanoDeTreinoJpa planoTreino;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="EXE_ATUAL") private @Nullable ExercicioJpa exercicioAtual;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="EXE_SUGERIDO") private @Nullable ExercicioJpa exercicioSugerido;
    @Column(name="REC_SERIES") private @Nullable Integer series;
    @Column(name="REC_REPETICOES_MINIMAS") private @Nullable Integer repeticoesMinimas;
    @Column(name="REC_REPETICOES_MAXIMAS") private @Nullable Integer repeticoesMaximas;
    @Column(name="REC_CARGA") private @Nullable BigDecimal carga;
    @Column(name="REC_INTERVALO_SEGUNDOS") private @Nullable Integer intervaloSegundos;
    @Column(name="AUC_ID") private @Nullable Long aula;
    @Column(name="PLA_ID") private @Nullable Long plano;
    @NotNull @Column(name="REC_GERACAO") private Instant geracao;
    @NotNull @Column(name="REC_VALIDADE") private LocalDate validade;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="REC_SITUACAO") private SituacaoRecomendacao situacao;
    @Column(name="REC_RESPOSTA") private @Nullable Instant resposta;
    @Column(name="REC_RESPONDENTE") private @Nullable String respondente;
    @Column(name="REC_MOTIVO") private @Nullable String motivo;
    @Column(name="REC_AJUSTE") private @Nullable String ajuste;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="PLT_GERADO") private @Nullable PlanoDeTreinoJpa planoGerado;
    @Column(name="REC_CIENCIA") private @Nullable Instant ciencia;
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="REV_EVIDENCIA", joinColumns=@JoinColumn(name="REC_ID"))
    @OrderColumn(name="REV_INDICE")
    private List<EvidenciaJpa> evidencias = new ArrayList<>();

    protected RecomendacaoJpa() { }

    public RecomendacaoJpa(@Nullable Long identificador, AlunoJpa aluno, CategoriaRecomendacao categoria,
            CriterioRecomendacao criterio, String item, String titulo, String descricao, String justificativa,
            @Nullable PersonalJpa personal, @Nullable PlanoDeTreinoJpa planoTreino, Instant geracao, LocalDate validade,
            SituacaoRecomendacao situacao) {
        this.geracao = geracao;
        this.validade = validade;
        this.situacao = situacao;
        this.identificador = identificador;
        this.aluno = aluno;
        this.categoria = categoria;
        this.criterio = criterio;
        this.item = item;
        this.titulo = titulo;
        this.descricao = descricao;
        this.justificativa = justificativa;
        this.personal = personal;
        this.planoTreino = planoTreino;
    }

    public void definirSugestao(@Nullable ExercicioJpa atual, @Nullable ExercicioJpa sugerido, @Nullable Integer quantidade,
            @Nullable Integer minimas, @Nullable Integer maximas, @Nullable BigDecimal novaCarga, @Nullable Integer intervalo) {
        this.exercicioAtual = atual;
        this.exercicioSugerido = sugerido;
        this.series = quantidade;
        this.repeticoesMinimas = minimas;
        this.repeticoesMaximas = maximas;
        this.carga = novaCarga;
        this.intervaloSegundos = intervalo;
    }

    public void definirReferencias(@Nullable Long novaAula, @Nullable Long novoPlano) {
        this.aula = novaAula;
        this.plano = novoPlano;
    }

    public void definirSituacao(Instant novaGeracao, LocalDate novaValidade, SituacaoRecomendacao novaSituacao,
            @Nullable Instant novaResposta, @Nullable String novoRespondente, @Nullable String novoMotivo,
            @Nullable String novoAjuste, @Nullable PlanoDeTreinoJpa novoPlanoGerado, @Nullable Instant novaCiencia) {
        this.geracao = novaGeracao;
        this.validade = novaValidade;
        this.situacao = novaSituacao;
        this.resposta = novaResposta;
        this.respondente = novoRespondente;
        this.motivo = novoMotivo;
        this.ajuste = novoAjuste;
        this.planoGerado = novoPlanoGerado;
        this.ciencia = novaCiencia;
    }

    public void definirEvidencias(List<EvidenciaJpa> novas) {
        this.evidencias = novas;
    }

    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public AlunoJpa getAluno() { return aluno; }
    public CategoriaRecomendacao getCategoria() { return categoria; }
    public CriterioRecomendacao getCriterio() { return criterio; }
    public String getItem() { return item; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getJustificativa() { return justificativa; }
    public @Nullable PersonalJpa getPersonal() { return personal; }
    public @Nullable PlanoDeTreinoJpa getPlanoTreino() { return planoTreino; }
    public @Nullable ExercicioJpa getExercicioAtual() { return exercicioAtual; }
    public @Nullable ExercicioJpa getExercicioSugerido() { return exercicioSugerido; }
    public @Nullable Integer getSeries() { return series; }
    public @Nullable Integer getRepeticoesMinimas() { return repeticoesMinimas; }
    public @Nullable Integer getRepeticoesMaximas() { return repeticoesMaximas; }
    public @Nullable BigDecimal getCarga() { return carga; }
    public @Nullable Integer getIntervaloSegundos() { return intervaloSegundos; }
    public @Nullable Long getAula() { return aula; }
    public @Nullable Long getPlano() { return plano; }
    public Instant getGeracao() { return geracao; }
    public LocalDate getValidade() { return validade; }
    public SituacaoRecomendacao getSituacao() { return situacao; }
    public @Nullable Instant getResposta() { return resposta; }
    public @Nullable String getRespondente() { return respondente; }
    public @Nullable String getMotivo() { return motivo; }
    public @Nullable String getAjuste() { return ajuste; }
    public @Nullable PlanoDeTreinoJpa getPlanoGerado() { return planoGerado; }
    public @Nullable Instant getCiencia() { return ciencia; }
    public List<EvidenciaJpa> getEvidencias() { return evidencias; }
}
