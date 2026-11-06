package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.treino;

import java.time.Instant;
import java.util.Objects;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.Treino;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.professor.ProfessorJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
@Entity @Table(name="TRN_TREINO")
public class TreinoJpa implements IAgregadoJpa<Treino> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="TRN_ID") private @Nullable Long identificador;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ALU_ID",nullable=false) private AlunoJpa aluno;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="PRO_ID",nullable=false) private ProfessorJpa professor;
    @Column(name="TRN_DESCRICAO",nullable=false,length=120) private String descricao;
    @Column(name="TRN_INICIO",nullable=false) private Instant inicio;
    @Column(name="TRN_TERMINO",nullable=false) private Instant termino;
    @Column(name="TRN_CONCLUIDO",nullable=false) private boolean concluido;
    @Column(name="TRN_DEMONSTRACAO",nullable=false) private boolean demonstracao;
    protected TreinoJpa(){}
    public TreinoJpa(@Nullable Long id,AlunoJpa aluno,ProfessorJpa professor,String descricao,Instant inicio,Instant termino,boolean concluido,boolean demonstracao){this.identificador=id;this.aluno=aluno;this.professor=professor;this.descricao=descricao;this.inicio=inicio;this.termino=termino;this.concluido=concluido;this.demonstracao=demonstracao;}
    public Long getIdentificador(){return Objects.requireNonNull(identificador);} public AlunoJpa getAluno(){return aluno;} public ProfessorJpa getProfessor(){return professor;}
    public String getDescricao(){return descricao;} public Instant getInicio(){return inicio;} public Instant getTermino(){return termino;} public boolean isConcluido(){return concluido;} public boolean isDemonstracao(){return demonstracao;}
}
