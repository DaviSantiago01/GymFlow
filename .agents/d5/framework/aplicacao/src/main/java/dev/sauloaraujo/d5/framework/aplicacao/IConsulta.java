package dev.sauloaraujo.d5.framework.aplicacao;

import java.util.Optional;

public interface IConsulta<O extends Enum<O> & IOrdenacao<?>> {
	Optional<O> getOrdenacao();

	boolean isDescendente();

	int getIndicePagina();

	int getTamanhoPagina();
}