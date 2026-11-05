package com.example.exemplo.dominio.geografia.unidadefederativa;

import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorRegexAbstrato;

public final class UnidadeFederativaSigla extends ObjetoValorRegexAbstrato {
	public static final int TAMANHO = 2;

	private static final String FORMATO = "[A-Z]{" + TAMANHO + "}";
	private static final String MENSAGEM = "A sigla da unidade federativa deve conter " + TAMANHO
			+ " letras maiúsculas";

	public UnidadeFederativaSigla(String valor) {
		super(valor, FORMATO, MENSAGEM);
	}
}