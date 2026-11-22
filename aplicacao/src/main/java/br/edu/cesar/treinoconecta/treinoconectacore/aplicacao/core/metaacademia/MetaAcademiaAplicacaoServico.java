package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.metaacademia;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.metaacademia.spi.IMetaAcademiaAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.MetaAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.MetaAcademiaAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.MetaAcademiaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.MetaAcademiaServico;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;

public final class MetaAcademiaAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<MetaAcademiaIdentificador,
        MetaAcademia, MetaAcademiaAutorizacoes, MetaAcademiaProjecao, MetaAcademiaConsulta> {
    public MetaAcademiaAplicacaoServico(IMetaAcademiaAplicacaoRepositorio repositorio, MetaAcademiaServico servico) {
        super(repositorio, servico);
    }
}
