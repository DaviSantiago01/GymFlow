package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.grupotreino;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.academia.AcademiaJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.SituacaoConviteTreino;
@Entity @Table(name="CTR_CONVITE_TREINO")
public class ConviteTreinoJpa {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="CTR_ID") @Nullable Long identificador;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="TGR_ID") TreinoGrupoJpa treino;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="ALU_ID") AlunoJpa aluno;
    @Enumerated(EnumType.STRING) @Column(name="CTR_SITUACAO",nullable=false,length=12) SituacaoConviteTreino situacao;
    @Column(name="CTR_CRIACAO",nullable=false) Instant criacao;
    @Column(name="CTR_RESPOSTA") @Nullable Instant resposta;
    @Column(name="CTR_CANCELAMENTO") @Nullable Instant cancelamento;
    @Column(name="CTR_MOTIVO",length=30) @Nullable String motivo;
    protected ConviteTreinoJpa(){}
}
