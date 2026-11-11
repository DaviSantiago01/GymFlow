package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.planodetreino;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.SituacaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.PersonalJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.jspecify.annotations.Nullable;

@Entity @Table(name="PLT_PLANO_TREINO")
public class PlanoDeTreinoJpa implements IAgregadoJpa<PlanoDeTreino> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="PLT_ID")
    private @Nullable Long identificador;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ALU_ID") private AlunoJpa aluno;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="PER_ID") private PersonalJpa personal;
    @NotNull @Size(max=PlanoDeTreino.TAMANHO_NOME) @Column(name="PLT_NOME") private String nome;
    @Column(name="PLT_FREQUENCIA_SEMANAL") private int frequenciaSemanal;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="PLT_SITUACAO") private SituacaoPlano situacao;
    @Column(name="PLT_VERSAO") private int versao;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="PLT_PLANO_ANTERIOR") private @Nullable PlanoDeTreinoJpa planoAnterior;
    @NotNull @Column(name="PLT_CRIACAO") private Instant criacao;
    @Column(name="PLT_ENCERRAMENTO") private @Nullable Instant encerramento;
    @OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL, orphanRemoval=true)
    @JoinColumn(name="PLT_ID", nullable=false)
    @OrderColumn(name="TRE_INDICE", nullable=false)
    private List<TreinoJpa> treinos = new ArrayList<>();

    protected PlanoDeTreinoJpa() { }

    public PlanoDeTreinoJpa(@Nullable Long identificador, AlunoJpa aluno, PersonalJpa personal, String nome,
            int frequenciaSemanal, SituacaoPlano situacao, int versao, @Nullable PlanoDeTreinoJpa planoAnterior,
            Instant criacao, @Nullable Instant encerramento, List<TreinoJpa> treinos) {
        this.identificador = identificador;
        this.aluno = aluno;
        this.personal = personal;
        this.nome = nome;
        this.frequenciaSemanal = frequenciaSemanal;
        this.situacao = situacao;
        this.versao = versao;
        this.planoAnterior = planoAnterior;
        this.criacao = criacao;
        this.encerramento = encerramento;
        this.treinos = treinos;
    }

    /** Os treinos de uma versão nunca mudam: só a situação é atualizada. */
    public void atualizarSituacao(SituacaoPlano novaSituacao, @Nullable Instant novoEncerramento) {
        this.situacao = novaSituacao;
        this.encerramento = novoEncerramento;
    }

    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public AlunoJpa getAluno() { return aluno; }
    public PersonalJpa getPersonal() { return personal; }
    public String getNome() { return nome; }
    public int getFrequenciaSemanal() { return frequenciaSemanal; }
    public SituacaoPlano getSituacao() { return situacao; }
    public int getVersao() { return versao; }
    public @Nullable PlanoDeTreinoJpa getPlanoAnterior() { return planoAnterior; }
    public Instant getCriacao() { return criacao; }
    public @Nullable Instant getEncerramento() { return encerramento; }
    public List<TreinoJpa> getTreinos() { return treinos; }
}
