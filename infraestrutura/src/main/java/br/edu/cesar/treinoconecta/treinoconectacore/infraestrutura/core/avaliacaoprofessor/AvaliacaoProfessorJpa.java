package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaoprofessor;

import java.time.Instant;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.treino.TreinoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor.AvaliacaoProfessor;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.professor.ProfessorJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
@Entity @Table(name="AVP_AVALIACAO_PROFESSOR",uniqueConstraints=@UniqueConstraint(name="UK_AVP_TREINO",columnNames={"TRN_ID"}))
public class AvaliacaoProfessorJpa implements IAgregadoJpa<AvaliacaoProfessor> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="AVP_ID") private @Nullable Long identificador;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ALU_ID") private AlunoJpa aluno;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="PRO_ID") private ProfessorJpa professor;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="TRN_ID") private @Nullable TreinoJpa treino;
    public @Nullable TreinoJpa getTreino(){return treino;}
    @Column(name="AVP_AUXILIO") private int auxilio;
    @Column(name="AVP_PONTUALIDADE") private int pontualidade;
    @Column(name="AVP_POSTURA") private int postura;
    @Column(name="AVP_DIDATICA") private int didatica;
    @Column(name="AVP_ADEQUACAO") private int adequacao;

    @Size(max=500) @Column(name="AVP_COMENTARIO") private @Nullable String comentario;
    @NotNull @Column(name="AVP_CRIACAO") private Instant criacao;
    @NotNull @Column(name="AVP_ATUALIZACAO") private Instant atualizacao;
    protected AvaliacaoProfessorJpa() { }
    public AvaliacaoProfessorJpa(@Nullable Long id,AlunoJpa aluno,ProfessorJpa professor,@Nullable TreinoJpa treino,int auxilio,int pontualidade,int postura,int didatica,int adequacao,@Nullable String comentario,Instant criacao,Instant atualizacao) {
        this.identificador=id;this.aluno=aluno;this.professor=professor;this.treino=treino;this.auxilio=auxilio;this.pontualidade=pontualidade;this.postura=postura;this.didatica=didatica;this.adequacao=adequacao;this.comentario=comentario;this.criacao=criacao;this.atualizacao=atualizacao;
    }
    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public AlunoJpa getAluno() { return aluno; } public ProfessorJpa getProfessor() { return professor; }
    public int getAuxilio() { return auxilio; }
    public int getPontualidade() { return pontualidade; }
    public int getPostura() { return postura; }
    public int getDidatica() { return didatica; }
    public int getAdequacao() { return adequacao; }

    public @Nullable String getComentario() { return comentario; } public Instant getCriacao() { return criacao; } public Instant getAtualizacao() { return atualizacao; }
}
