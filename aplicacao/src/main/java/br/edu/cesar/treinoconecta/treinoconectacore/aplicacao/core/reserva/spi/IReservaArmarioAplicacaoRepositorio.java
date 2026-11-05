package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.reserva.spi;

import java.time.Instant;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.reserva.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.ReservaArmarioIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;
public interface IReservaArmarioAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<ReservaArmarioProjecao,ReservaArmarioConsulta> {
    Optional<ReservaArmarioProjecao> ativa(String usuario, Instant agora);
    ReservaArmarioProjecao detalhe(ReservaArmarioIdentificador id, String usuario, Instant agora);
}
