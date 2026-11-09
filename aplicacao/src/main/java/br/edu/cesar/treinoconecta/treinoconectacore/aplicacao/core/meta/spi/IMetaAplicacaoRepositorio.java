package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.MetaConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.MetaProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.MetaIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IMetaAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<MetaProjecao, MetaConsulta> {
    MetaProjecao projetar(MetaIdentificador identificador);
}
