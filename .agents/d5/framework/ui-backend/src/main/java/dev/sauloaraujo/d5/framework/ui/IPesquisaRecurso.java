package dev.sauloaraujo.d5.framework.ui;

import dev.sauloaraujo.d5.framework.aplicacao.IConsulta;
import dev.sauloaraujo.d5.framework.aplicacao.IConsultaResultado;
import dev.sauloaraujo.d5.framework.aplicacao.IOrdenacao;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public interface IPesquisaRecurso<P extends IProjecao<?>, C extends IConsulta<? extends IOrdenacao<P>>> {
	C getConsulta();

	IConsultaResultado<P> getResultado();
}