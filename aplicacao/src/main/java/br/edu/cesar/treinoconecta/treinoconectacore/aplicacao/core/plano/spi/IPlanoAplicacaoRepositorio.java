package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.PlanoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.PlanoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IPlanoAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<PlanoProjecao, PlanoConsulta> {
    PlanoProjecao projetar(PlanoIdentificador identificador);
}
