package com.example.exemplo.dominio.geografia.municipio;

import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaCodigoIbge;

import dev.sauloaraujo.d5.framework.dominio.IIdentificador;
import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorRegexAbstrato;

public final class MunicipioCodigoIbge extends ObjetoValorRegexAbstrato implements IIdentificador {
	public static final int TAMANHO = 7;

	private static final String FORMATO = "\\d{" + TAMANHO + "}";
	private static final String MENSAGEM = "O código IBGE do município deve conter " + TAMANHO + " dígitos";

	public MunicipioCodigoIbge(String valor) {
		super(valor, FORMATO, MENSAGEM);
	}

	public UnidadeFederativaCodigoIbge getUnidadeFederativa() {
		return new UnidadeFederativaCodigoIbge(toString().substring(0, UnidadeFederativaCodigoIbge.TAMANHO));
	}
}