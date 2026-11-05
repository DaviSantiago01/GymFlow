package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.ArmarioCodigo;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;

/** Agregado imutável. O encerramento real por vencimento é o prazo, mesmo após indisponibilidade do servidor. */
public final class ReservaArmario implements IAgregado<ReservaArmarioIdentificador> {
    public static final Duration DURACAO_MAXIMA = Duration.ofHours(3);
    private final Optional<ReservaArmarioIdentificador> identificador;
    private final AlunoIdentificador aluno;
    private final ArmarioCodigo armario;
    private final Instant confirmacao;
    private final Instant inicio;
    private final Instant termino;
    private final Optional<Instant> encerramento;
    private final SituacaoReserva situacao;

    public ReservaArmario(Optional<ReservaArmarioIdentificador> identificador, AlunoIdentificador aluno,
            ArmarioCodigo armario, Instant confirmacao, Instant inicio, Instant termino, Optional<Instant> encerramento,
            SituacaoReserva situacao) {
        this.identificador = Objects.requireNonNull(identificador);
        this.aluno = Objects.requireNonNull(aluno);
        this.armario = Objects.requireNonNull(armario);
        this.confirmacao = Objects.requireNonNull(confirmacao);
        this.inicio = Objects.requireNonNull(inicio);
        this.termino = Objects.requireNonNull(termino);
        this.encerramento = Objects.requireNonNull(encerramento);
        this.situacao = Objects.requireNonNull(situacao);
        Validate.isTrue(!inicio.isBefore(confirmacao), "O início não pode anteceder a confirmação");
        var duracao = Duration.between(inicio, termino);
        Validate.isTrue(duracao.equals(Duration.ofHours(1)) || duracao.equals(Duration.ofHours(2)) || duracao.equals(DURACAO_MAXIMA),
                "A duração deve ser de uma a três horas inteiras");
        Validate.isTrue((situacao == SituacaoReserva.ATIVA) == encerramento.isEmpty(), "Encerramento inconsistente");
        encerramento.ifPresent(valor -> {
            Validate.isTrue(!valor.isBefore(confirmacao) && !valor.isAfter(termino), "Horário de encerramento inválido");
            Validate.isTrue(situacao != SituacaoReserva.VENCIDA || valor.equals(termino), "Vencimento inconsistente");
            Validate.isTrue(situacao != SituacaoReserva.ENCERRADA || !valor.isBefore(inicio), "O uso ainda não começou");
        });
    }
    public static ReservaArmario confirmar(AlunoIdentificador aluno, ArmarioCodigo armario, Instant agora, PeriodoReserva periodo) {
        Objects.requireNonNull(periodo);
        Validate.isTrue(!periodo.inicio().isBefore(Objects.requireNonNull(agora)), "O horário selecionado já passou");
        return new ReservaArmario(Optional.empty(), aluno, armario, agora, periodo.inicio(), periodo.termino(),
                Optional.empty(), SituacaoReserva.ATIVA);
    }
    public boolean ativaEm(Instant agora) {
        return situacao == SituacaoReserva.ATIVA && Objects.requireNonNull(agora).isBefore(termino);
    }
    public ReservaArmario vencer(Instant agora) {
        Objects.requireNonNull(agora);
        if (situacao != SituacaoReserva.ATIVA || agora.isBefore(termino)) return this;
        return finalizar(SituacaoReserva.VENCIDA, termino);
    }
    public ReservaArmario cancelar(AlunoIdentificador responsavel, Instant agora) {
        return encerrarPeloResponsavel(responsavel, agora, SituacaoReserva.CANCELADA);
    }
    public ReservaArmario encerrar(AlunoIdentificador responsavel, Instant agora) {
        return encerrarPeloResponsavel(responsavel, agora, SituacaoReserva.ENCERRADA);
    }
    private ReservaArmario encerrarPeloResponsavel(AlunoIdentificador responsavel, Instant agora, SituacaoReserva destino) {
        Objects.requireNonNull(responsavel);
        Objects.requireNonNull(agora);
        if (!aluno.equals(responsavel)) throw new NaoAutorizadoExcecao("Esta reserva pertence a outro aluno");
        Validate.validState(ativaEm(agora), "A reserva já foi finalizada");
        Validate.validState(!agora.isBefore(confirmacao), "Horário anterior à confirmação");
        Validate.validState(destino != SituacaoReserva.ENCERRADA || !agora.isBefore(inicio), "O uso do armário ainda não começou; você pode cancelar a reserva");
        return finalizar(destino, agora);
    }
    private ReservaArmario finalizar(SituacaoReserva destino, Instant quando) {
        return new ReservaArmario(identificador, aluno, armario, confirmacao, inicio, termino, Optional.of(quando), destino);
    }
    public Optional<ReservaArmarioIdentificador> getIdentificador() { return identificador; }
    public AlunoIdentificador getAluno() { return aluno; }
    public ArmarioCodigo getArmario() { return armario; }
    public Instant getConfirmacao() { return confirmacao; }
    public Instant getInicio() { return inicio; }
    public Instant getTermino() { return termino; }
    public Optional<Instant> getEncerramento() { return encerramento; }
    public SituacaoReserva getSituacao() { return situacao; }
}
