package dev.sauloaraujo.d5.framework.dominio;

import org.jspecify.annotations.Nullable;

public interface IObjetoValor {
	@Override
	boolean equals(@Nullable Object objeto);

	@Override
	int hashCode();
}