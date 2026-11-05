package dev.sauloaraujo.d5.framework.ui;

import java.util.Objects;

import dev.sauloaraujo.d5.framework.dominio.IAgregado;
import dev.sauloaraujo.d5.framework.dominio.IAgregadoAutorizacoes;

public class AgregadoRecurso<A extends IAgregado<?>, U extends IAgregadoAutorizacoes<A>>
		implements IAgregadoRecurso<A, U> {
	private final A agregado;
	private final U autorizacoes;

	public AgregadoRecurso(A agregado, U autorizacoes) {
		this.agregado = Objects.requireNonNull(agregado);
		this.autorizacoes = Objects.requireNonNull(autorizacoes);
	}

	public A getAgregado() {
		return agregado;
	}

	@Override
	public U getAutorizacoes() {
		return autorizacoes;
	}
}