package com.example.exemplo.aplicacao.geografia.municipio;

import java.util.Objects;
import java.util.Optional;

import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class MunicipioConsulta extends Consulta<MunicipioOrdenacao> {
	private final Optional<String> nome;
	private final Optional<String> unidadeFederativa;

	public MunicipioConsulta(Optional<MunicipioOrdenacao> ordenacao, Optional<Boolean> descendente,
			Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina, Optional<String> nome,
			Optional<String> unidadeFederativa) {
		super(ordenacao, descendente, indicePagina, tamanhoPagina);

		this.nome = Objects.requireNonNull(nome);
		this.unidadeFederativa = Objects.requireNonNull(unidadeFederativa);
	}

	public Optional<String> getNome() {
		return nome;
	}

	public Optional<String> getUnidadeFederativa() {
		return unidadeFederativa;
	}
}