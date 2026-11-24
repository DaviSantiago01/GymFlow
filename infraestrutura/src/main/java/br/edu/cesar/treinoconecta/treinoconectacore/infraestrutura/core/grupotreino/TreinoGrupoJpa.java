package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.grupotreino;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.academia.AcademiaJpa;
@Entity @Table(name="TGR_TREINO_GRUPO")
public class TreinoGrupoJpa {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="TGR_ID") @Nullable Long identificador;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="GRT_ID") GrupoTreinoJpa grupo;
    @Column(name="TGR_INICIO",nullable=false) Instant inicio;
    @Column(name="TGR_PRAZO",nullable=false) Instant prazo;
    @Column(name="TGR_CRIACAO",nullable=false) Instant criacao;
    @Column(name="TGR_CANCELAMENTO") @Nullable Instant cancelamento;
    @Column(name="PRF_ID") @Nullable Long professor;
    @OneToMany(mappedBy="treino",cascade={CascadeType.PERSIST,CascadeType.MERGE}) @OrderBy("identificador") List<ConviteTreinoJpa> convites=new ArrayList<>();
    protected TreinoGrupoJpa(){}
}
