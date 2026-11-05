package dev.sauloaraujo.d5.framework.aplicacao.spi;

import dev.sauloaraujo.d5.framework.aplicacao.IConsulta;
import dev.sauloaraujo.d5.framework.aplicacao.IConsultaResultado;
import dev.sauloaraujo.d5.framework.aplicacao.IOrdenacao;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public interface IAgregadoAplicacaoRepositorio<P extends IProjecao<?>, C extends IConsulta<? extends IOrdenacao<P>>> {
	IConsultaResultado<P> pesquisar(C consulta);
}