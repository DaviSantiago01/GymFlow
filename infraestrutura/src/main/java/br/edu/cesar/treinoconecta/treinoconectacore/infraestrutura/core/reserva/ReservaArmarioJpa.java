package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.reserva;

import java.time.Instant;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.*;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.armario.ArmarioJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
@Entity @Table(name="RSV_RESERVA_ARMARIO")
public class ReservaArmarioJpa implements IAgregadoJpa<ReservaArmario> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="RSV_ID") private @Nullable Long identificador;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ALU_ID") private AlunoJpa aluno;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ARM_CODIGO") private ArmarioJpa armario;
    @NotNull @Column(name="RSV_CONFIRMACAO") private Instant confirmacao;
    @NotNull @Column(name="RSV_INICIO") private Instant inicio;
    @NotNull @Column(name="RSV_TERMINO") private Instant termino;
    @Column(name="RSV_ENCERRAMENTO") private @Nullable Instant encerramento;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="RSV_SITUACAO") private SituacaoReserva situacao;
    protected ReservaArmarioJpa() { }
    public ReservaArmarioJpa(@Nullable Long identificador, AlunoJpa aluno, ArmarioJpa armario, Instant confirmacao,
            Instant inicio, Instant termino, @Nullable Instant encerramento, SituacaoReserva situacao) {
        this.identificador=identificador; this.aluno=aluno; this.armario=armario;
        this.inicio=inicio; this.confirmacao=confirmacao; this.termino=termino; this.encerramento=encerramento; this.situacao=situacao;
    }
    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public AlunoJpa getAluno() { return aluno; }
    public ArmarioJpa getArmario() { return armario; }
    public Instant getConfirmacao() { return confirmacao; }
    public Instant getInicio() { return inicio; }
    public Instant getTermino() { return termino; }
    public @Nullable Instant getEncerramento() { return encerramento; }
    public SituacaoReserva getSituacao() { return situacao; }
}
