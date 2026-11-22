package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.metaacademia.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.metaacademia.MetaAcademiaConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.metaacademia.MetaAcademiaProjecao;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IMetaAcademiaAplicacaoRepositorio
        extends IAgregadoAplicacaoRepositorio<MetaAcademiaProjecao, MetaAcademiaConsulta> {
}
