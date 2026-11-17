package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.grupotreino;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.academia.AcademiaJpa;
@Entity @Table(name="PGR_PARTICIPACAO_GRUPO")
public class ParticipacaoGrupoJpa {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="PGR_ID") @Nullable Long identificador;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="GRT_ID") GrupoTreinoJpa grupo;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="ALU_ID") AlunoJpa aluno;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="ACD_ID") AcademiaJpa academia;
    @Column(name="PGR_ENTRADA",nullable=false) Instant entrada;
    @Column(name="PGR_SAIDA") @Nullable Instant saida;
    protected ParticipacaoGrupoJpa(){}
}
