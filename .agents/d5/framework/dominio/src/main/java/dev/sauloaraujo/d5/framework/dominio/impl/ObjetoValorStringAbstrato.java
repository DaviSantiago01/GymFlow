package dev.sauloaraujo.d5.framework.dominio.impl;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

public abstract class ObjetoValorStringAbstrato implements IObjetoValor {
	private final String valor;

	protected ObjetoValorStringAbstrato(String valor) {
		this.valor = Objects.requireNonNull(valor);
	}

	@Override
	public boolean equals(@Nullable Object objeto) {
		if (objeto == null || getClass() != objeto.getClass()) {
			return false;
		}
		var outro = (ObjetoValorStringAbstrato) objeto;
		return Objects.equals(valor, outro.valor);
	}

	@Override
	public int hashCode() {
		return valor.hashCode();
	}

	@Override
	public String toString() {
		return valor;
	}
}