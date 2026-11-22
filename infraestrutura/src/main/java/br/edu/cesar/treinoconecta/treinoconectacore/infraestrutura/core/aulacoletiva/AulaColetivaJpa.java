package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aulacoletiva;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.AulaColetiva;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.ImpactoAula;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.NivelExperiencia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity @Table(name="AUC_AULA_COLETIVA")
public class AulaColetivaJpa implements IAgregadoJpa<AulaColetiva> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="AUC_ID")
    private @Nullable Long identificador;
    @NotNull @Column(name="AUC_NOME") private String nome;
    @NotNull @Column(name="AUC_MODALIDADE") private String modalidade;
    @NotNull @Column(name="AUC_INSTRUTOR") private String instrutor;
    @NotNull @Column(name="AUC_SALA") private String sala;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="AUC_DIA") private DiaSemana dia;
    @NotNull @Convert(converter=HorarioLocalConversor.class) @Column(name="AUC_INICIO") private LocalTime inicio;
    @NotNull @Convert(converter=HorarioLocalConversor.class) @Column(name="AUC_FIM") private LocalTime fim;
    @Column(name="AUC_CAPACIDADE") private int capacidade;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="AUC_NIVEL") private NivelExperiencia nivel;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="AUC_IMPACTO") private ImpactoAula impacto;
    @NotNull @Column(name="AUC_OBJETIVOS") private String objetivos;
    @NotNull @Column(name="AUC_CONTRAINDICACOES") private String contraindicacoes;
    @Column(name="AUC_ATIVA") private boolean ativa;
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="IAC_INSCRICAO_AULA", joinColumns=@JoinColumn(name="AUC_ID"))
    private List<InscricaoAulaJpa> inscricoes = new ArrayList<>();

    protected AulaColetivaJpa() { }

    public AulaColetivaJpa(@Nullable Long identificador, String nome, String modalidade, String instrutor, String sala,
            DiaSemana dia, LocalTime inicio, LocalTime fim, int capacidade, NivelExperiencia nivel, ImpactoAula impacto,
            String objetivos, String contraindicacoes, boolean ativa, List<InscricaoAulaJpa> inscricoes) {
        this.identificador = identificador;
        this.nome = nome;
        this.modalidade = modalidade;
        this.instrutor = instrutor;
        this.sala = sala;
        this.dia = dia;
        this.inicio = inicio;
        this.fim = fim;
        this.capacidade = capacidade;
        this.nivel = nivel;
        this.impacto = impacto;
        this.objetivos = objetivos;
        this.contraindicacoes = contraindicacoes;
        this.ativa = ativa;
        this.inscricoes = inscricoes;
    }

    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public String getNome() { return nome; }
    public String getModalidade() { return modalidade; }
    public String getInstrutor() { return instrutor; }
    public String getSala() { return sala; }
    public DiaSemana getDia() { return dia; }
    public LocalTime getInicio() { return inicio; }
    public LocalTime getFim() { return fim; }
    public int getCapacidade() { return capacidade; }
    public NivelExperiencia getNivel() { return nivel; }
    public ImpactoAula getImpacto() { return impacto; }
    public String getObjetivos() { return objetivos; }
    public String getContraindicacoes() { return contraindicacoes; }
    public boolean isAtiva() { return ativa; }
    public List<InscricaoAulaJpa> getInscricoes() { return inscricoes; }
}
