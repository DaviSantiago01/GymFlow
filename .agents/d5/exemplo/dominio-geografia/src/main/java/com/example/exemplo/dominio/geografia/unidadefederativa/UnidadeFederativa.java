package com.example.exemplo.dominio.geografia.unidadefederativa;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import dev.sauloaraujo.d5.framework.dominio.IAgregado;

public final class UnidadeFederativa implements IAgregado<UnidadeFederativaCodigoIbge> {
	private final UnidadeFederativaCodigoIbge identificador;
	private final UnidadeFederativaSigla sigla;
	private final String nome;

	public UnidadeFederativa(UnidadeFederativaCodigoIbge identificador, UnidadeFederativaSigla sigla, String nome) {
		this.identificador = Objects.requireNonNull(identificador);
		this.sigla = Objects.requireNonNull(sigla);
		this.nome = Validate.notBlank(nome, "Nome da unidade federativa em branco");
	}

	@Override
	public Optional<UnidadeFederativaCodigoIbge> getIdentificador() {
		return Optional.of(identificador);
	}

	public UnidadeFederativaSigla getSigla() {
		return sigla;
	}

	public String getNome() {
		return nome;
	}
}