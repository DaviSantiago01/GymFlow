package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.grupotreino;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.academia.AcademiaJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.GrupoTreino;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
@Entity @Table(name="GRT_GRUPO_TREINO")
public class GrupoTreinoJpa implements IAgregadoJpa<GrupoTreino> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="GRT_ID") @Nullable Long identificador;
    @Column(name="GRT_NOME",nullable=false,length=60) String nome;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="ALU_ORGANIZADOR") AlunoJpa organizador;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="ACD_ID") AcademiaJpa academia;
    @Column(name="GRT_CRIACAO",nullable=false) Instant criacao;
    @Column(name="GRT_ENCERRAMENTO") @Nullable Instant encerramento;
    @OneToMany(mappedBy="grupo",cascade={CascadeType.PERSIST,CascadeType.MERGE}) @OrderBy("identificador") List<ParticipacaoGrupoJpa> participacoes=new ArrayList<>();
    @OneToMany(mappedBy="grupo",cascade={CascadeType.PERSIST,CascadeType.MERGE}) @OrderBy("identificador") List<ConviteGrupoJpa> convites=new ArrayList<>();
    @OneToMany(mappedBy="grupo",cascade={CascadeType.PERSIST,CascadeType.MERGE}) @OrderBy("identificador") List<TreinoGrupoJpa> treinos=new ArrayList<>();
    protected GrupoTreinoJpa(){}
}
