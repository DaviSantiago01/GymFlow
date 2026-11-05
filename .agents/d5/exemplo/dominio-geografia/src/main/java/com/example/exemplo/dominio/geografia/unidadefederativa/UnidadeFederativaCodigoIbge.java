package com.example.exemplo.dominio.geografia.unidadefederativa;

import dev.sauloaraujo.d5.framework.dominio.IIdentificador;
import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorRegexAbstrato;

public final class UnidadeFederativaCodigoIbge extends ObjetoValorRegexAbstrato implements IIdentificador {
	public static final int TAMANHO = 2;

	private static final String FORMATO = "\\d{" + TAMANHO + "}";
	private static final String MENSAGEM = "O código IBGE da unidade federativa deve conter " + TAMANHO + " dígitos";

	public UnidadeFederativaCodigoIbge(String valor) {
		super(valor, FORMATO, MENSAGEM);
	}
}