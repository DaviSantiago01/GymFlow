package dev.sauloaraujo.d5.framework.aplicacao.impl;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import dev.sauloaraujo.d5.framework.aplicacao.IConsulta;
import dev.sauloaraujo.d5.framework.aplicacao.IOrdenacao;

public class Consulta<O extends Enum<O> & IOrdenacao<?>> implements IConsulta<O> {
	public static final int TAMANHO_PAGINA_PADRAO = 10;
	public static final int TAMANHO_PAGINA_MAXIMO = 1000;

	private final Optional<O> ordenacao;
	private final boolean descendente;
	private final int indicePagina;
	private final int tamanhoPagina;

	public Consulta(Optional<O> ordenacao, Optional<Boolean> descendente, Optional<Integer> indicePagina,
			Optional<Integer> tamanhoPagina) {
		this.ordenacao = Objects.requireNonNull(ordenacao);

		Objects.requireNonNull(descendente);
		this.descendente = descendente.orElse(false);

		Objects.requireNonNull(indicePagina);
		indicePagina.ifPresent(valor -> Validate.isTrue(valor >= 0, "Índice da página deve ser maior ou igual a zero"));
		this.indicePagina = indicePagina.orElse(0);

		Objects.requireNonNull(tamanhoPagina);
		tamanhoPagina.ifPresent(valor -> Validate.isTrue(valor > 0 && valor <= TAMANHO_PAGINA_MAXIMO,
				"Tamanho da página deve estar entre 1 e %d", TAMANHO_PAGINA_MAXIMO));
		this.tamanhoPagina = tamanhoPagina.orElse(TAMANHO_PAGINA_PADRAO);
	}

	@Override
	public Optional<O> getOrdenacao() {
		return ordenacao;
	}

	@Override
	public boolean isDescendente() {
		return descendente;
	}

	@Override
	public int getIndicePagina() {
		return indicePagina;
	}

	@Override
	public int getTamanhoPagina() {
		return tamanhoPagina;
	}
}
