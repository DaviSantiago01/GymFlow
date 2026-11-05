package dev.sauloaraujo.d5.framework.ui;

import java.util.Objects;

import dev.sauloaraujo.d5.framework.aplicacao.IConsulta;
import dev.sauloaraujo.d5.framework.aplicacao.IConsultaResultado;
import dev.sauloaraujo.d5.framework.aplicacao.IOrdenacao;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public class PesquisaRecurso<P extends IProjecao<?>, C extends IConsulta<? extends IOrdenacao<P>>>
		implements IPesquisaRecurso<P, C> {
	private final C consulta;
	private final IConsultaResultado<P> resultado;

	public PesquisaRecurso(C consulta, IConsultaResultado<P> resultado) {
		this.consulta = Objects.requireNonNull(consulta);
		this.resultado = Objects.requireNonNull(resultado);
	}

	public C getConsulta() {
		return consulta;
	}

	public IConsultaResultado<P> getResultado() {
		return resultado;
	}
}