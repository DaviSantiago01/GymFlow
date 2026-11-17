package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano.spi;

import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano.AdesaoPlanoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano.AdesaoPlanoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IAdesaoPlanoAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<AdesaoPlanoProjecao, AdesaoPlanoConsulta> {
    AdesaoPlanoProjecao projetar(AdesaoPlanoIdentificador identificador);
    Optional<AdesaoPlanoProjecao> projetarAtivaDoUsuario(String usuario);
}
