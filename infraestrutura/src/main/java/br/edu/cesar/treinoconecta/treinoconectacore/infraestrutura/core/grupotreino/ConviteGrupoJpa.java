package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.grupotreino;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.academia.AcademiaJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.SituacaoConviteGrupo;
@Entity @Table(name="CGR_CONVITE_GRUPO")
public class ConviteGrupoJpa {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="CGR_ID") @Nullable Long identificador;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="GRT_ID") GrupoTreinoJpa grupo;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="ALU_ID") AlunoJpa aluno;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="ACD_ID") AcademiaJpa academia;
    @Enumerated(EnumType.STRING) @Column(name="CGR_SITUACAO",nullable=false,length=12) SituacaoConviteGrupo situacao;
    @Column(name="CGR_ENVIO",nullable=false) Instant envio;
    @Column(name="CGR_RESPOSTA") @Nullable Instant resposta;
    protected ConviteGrupoJpa(){}
}
