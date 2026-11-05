package com.example.exemplo.aplicacao.crm.cliente;

import java.util.Objects;
import java.util.Optional;

import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class ClienteConsulta extends Consulta<ClienteOrdenacao> {
	private final Optional<String> nome;
	private final Optional<String> municipio;
	private final Optional<String> unidadeFederativa;

	public ClienteConsulta(Optional<ClienteOrdenacao> ordenacao, Optional<Boolean> descendente,
			Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina, Optional<String> nome,
			Optional<String> municipio, Optional<String> unidadeFederativa) {
		super(ordenacao, descendente, indicePagina, tamanhoPagina);

		this.nome = Objects.requireNonNull(nome);
		this.municipio = Objects.requireNonNull(municipio);
		this.unidadeFederativa = Objects.requireNonNull(unidadeFederativa);
	}

	public Optional<String> getNome() {
		return nome;
	}

	public Optional<String> getMunicipio() {
		return municipio;
	}

	public Optional<String> getUnidadeFederativa() {
		return unidadeFederativa;
	}
}
