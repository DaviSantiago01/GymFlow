package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva;

import java.time.Clock;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.ArmarioCodigo;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.spi.IArmarioRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.spi.IReservaArmarioRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.*;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;

public final class ReservaArmarioServico extends AgregadoServicoAbstrato<ReservaArmarioIdentificador,
        ReservaArmario, AgregadoAutorizacoes<ReservaArmario>> {
    private final IReservaArmarioRepositorio reservas;
    private final IAlunoRepositorio alunos;
    private final IArmarioRepositorio armarios;
    private final Clock relogio;
    public ReservaArmarioServico(ISegurancaServico seguranca, IReservaArmarioRepositorio reservas,
            IAlunoRepositorio alunos, IArmarioRepositorio armarios, Clock relogio) {
        super(seguranca, reservas, "Reserva %s existente", "Reserva %s inexistente");
        this.reservas = Objects.requireNonNull(reservas);
        this.alunos = Objects.requireNonNull(alunos);
        this.armarios = Objects.requireNonNull(armarios);
        this.relogio = Objects.requireNonNull(relogio);
    }
    @Override public AgregadoAutorizacoes<ReservaArmario> criarAutorizacoes(Optional<ReservaArmarioIdentificador> id) {
        Objects.requireNonNull(id);
        boolean permitido = getSegurancaServico().possuiAutorizacao("USUARIO");
        if (permitido && id.isPresent()) {
            var aluno = alunos.buscarPorUsuario(getSegurancaServico().getUsuario());
            permitido = reservas.buscarPorIdentificador(id.get()).getAluno().equals(aluno.getIdentificador().orElseThrow());
        }
        // Os comandos específicos preservam os horários e o histórico; CRUD genérico não os pode reescrever.
        return new AgregadoAutorizacoes<>(false, permitido, false, false);
    }
    public ReservaArmario reservar(ArmarioCodigo codigo, java.time.LocalDate data, int hora, int duracaoHoras) {
        Objects.requireNonNull(codigo);
        validarAlunoAutenticado();
        // Ordem fixa de bloqueio: aluno, armário, reservas. A transação é iniciada pelo D5 no POST.
        var aluno = alunos.bloquearPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow();
        armarios.bloquear(codigo);
        var agora = relogio.instant();
        var periodo = PeriodoReserva.criar(data, hora, duracaoHoras);
        var nova = ReservaArmario.confirmar(aluno, codigo, agora, periodo);
        reservas.vencerRelacionadas(aluno, codigo, agora);
        Validate.validState(!reservas.existeAtivaPorAluno(aluno, agora), "Você já possui uma reserva em uso ou agendada");
        Validate.validState(!reservas.existeSobreposicao(codigo, periodo.inicio(), periodo.termino()), "Este horário não está mais disponível. Escolha outro período");
        return reservas.incluir(nova);
    }
    public ReservaArmario cancelar(ReservaArmarioIdentificador id) { return finalizar(id, true); }
    public ReservaArmario encerrar(ReservaArmarioIdentificador id) { return finalizar(id, false); }
    private ReservaArmario finalizar(ReservaArmarioIdentificador id, boolean cancelamento) {
        Objects.requireNonNull(id);
        validarAlunoAutenticado();
        var aluno = alunos.bloquearPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow();
        var reserva = reservas.bloquear(id);
        var agora = relogio.instant();
        var finalizada = cancelamento ? reserva.cancelar(aluno, agora) : reserva.encerrar(aluno, agora);
        reservas.alterar(finalizada);
        return finalizada;
    }
    private void validarAlunoAutenticado() {
        if (!getSegurancaServico().possuiAutorizacao("USUARIO")) throw new NaoAutorizadoExcecao("Acesso exclusivo de alunos");
    }
}
