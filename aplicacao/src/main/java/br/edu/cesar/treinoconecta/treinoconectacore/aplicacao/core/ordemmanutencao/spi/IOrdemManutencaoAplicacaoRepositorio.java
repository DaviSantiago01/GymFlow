package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.ordemmanutencao.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.ordemmanutencao.OrdemManutencaoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.ordemmanutencao.OrdemManutencaoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.OrdemManutencaoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IOrdemManutencaoAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<OrdemManutencaoProjecao, OrdemManutencaoConsulta> {
    OrdemManutencaoProjecao projetar(OrdemManutencaoIdentificador identificador);
}
