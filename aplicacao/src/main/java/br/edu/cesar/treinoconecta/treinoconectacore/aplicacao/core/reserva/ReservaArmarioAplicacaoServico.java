package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.reserva;

import java.time.Clock;
import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.RespostaTemporal;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.reserva.spi.IReservaArmarioAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.*;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
public final class ReservaArmarioAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<ReservaArmarioIdentificador,ReservaArmario,AgregadoAutorizacoes<ReservaArmario>,ReservaArmarioProjecao,ReservaArmarioConsulta> {
    private final IReservaArmarioAplicacaoRepositorio repositorio;
    private final ReservaArmarioServico servico;
    private final Clock relogio;
    private final ISegurancaServico seguranca;
    public ReservaArmarioAplicacaoServico(IReservaArmarioAplicacaoRepositorio repositorio, ReservaArmarioServico servico, Clock relogio, ISegurancaServico seguranca) {
        super(repositorio,servico); this.repositorio=Objects.requireNonNull(repositorio);
        this.servico=Objects.requireNonNull(servico); this.relogio=Objects.requireNonNull(relogio); this.seguranca=Objects.requireNonNull(seguranca);
    }
    
    public RespostaTemporal<Optional<ReservaArmarioProjecao>> ativa() {
        autorizar();
        var agora=relogio.instant();
        return new RespostaTemporal<>(repositorio.ativa(seguranca.getUsuario(),agora),agora);
    }
    public RespostaTemporal<ReservaArmarioProjecao> detalhe(ReservaArmarioIdentificador id) {
        autorizar();
        var agora=relogio.instant();
        return new RespostaTemporal<>(repositorio.detalhe(id,seguranca.getUsuario(),agora),agora);
    }
    private void autorizar() {
        if (!seguranca.possuiAutorizacao("USUARIO")) throw new dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao("Acesso exclusivo de alunos");
    }

}
