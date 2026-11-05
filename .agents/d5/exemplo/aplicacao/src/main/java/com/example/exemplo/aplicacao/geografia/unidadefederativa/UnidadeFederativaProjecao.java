package com.example.exemplo.aplicacao.geografia.unidadefederativa;

import java.util.Objects;

import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativa;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaCodigoIbge;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaSigla;

import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public final class UnidadeFederativaProjecao implements IProjecao<UnidadeFederativa> {
	private final UnidadeFederativaCodigoIbge identificador;
	private final UnidadeFederativaSigla sigla;
	private final String nome;

	public UnidadeFederativaProjecao(UnidadeFederativaCodigoIbge identificador, UnidadeFederativaSigla sigla,
			String nome) {
		this.identificador = Objects.requireNonNull(identificador);
		this.sigla = Objects.requireNonNull(sigla);
		this.nome = Objects.requireNonNull(nome);
	}

	public UnidadeFederativaProjecao(String identificador, String sigla, String nome) {
		this(new UnidadeFederativaCodigoIbge(identificador), new UnidadeFederativaSigla(sigla), nome);
	}

	public UnidadeFederativaCodigoIbge getIdentificador() {
		return identificador;
	}

	public UnidadeFederativaSigla getSigla() {
		return sigla;
	}

	public String getNome() {
		return nome;
	}
}