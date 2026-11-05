package com.example.exemplo.dominio.crm.cliente;

import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorRegexAbstrato;

public final class Cpf extends ObjetoValorRegexAbstrato {
	public static final int TAMANHO = 11;

	private static final String FORMATO = "\\d{" + TAMANHO + "}";
	private static final String MENSAGEM = "O CPF deve conter " + TAMANHO + " dígitos";

	public Cpf(String valor) {
		super(valor, FORMATO, MENSAGEM);
	}
}
