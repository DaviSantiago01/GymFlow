package com.example.exemplo.aplicacao.geografia.unidadefederativa;

import java.util.Objects;
import java.util.Optional;

import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class UnidadeFederativaConsulta extends Consulta<UnidadeFederativaOrdenacao> {
	private final Optional<String> sigla;
	private final Optional<String> nome;

	public UnidadeFederativaConsulta(Optional<UnidadeFederativaOrdenacao> ordenacao, Optional<Boolean> descendente,
			Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina, Optional<String> sigla,
			Optional<String> nome) {
		super(ordenacao, descendente, indicePagina, tamanhoPagina);

		this.sigla = Objects.requireNonNull(sigla);
		this.nome = Objects.requireNonNull(nome);
	}

	public Optional<String> getSigla() {
		return sigla;
	}

	public Optional<String> getNome() {
		return nome;
	}
}