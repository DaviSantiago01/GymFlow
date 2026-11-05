package com.example.exemplo.dominio.crm.cliente;

import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorRegexAbstrato;

public final class EnderecoCep extends ObjetoValorRegexAbstrato {
	public static final int TAMANHO = 8;

	private static final String FORMATO = "\\d{" + TAMANHO + "}";
	private static final String MENSAGEM = "O CEP do endereço deve conter " + TAMANHO + " dígitos";

	public EnderecoCep(String valor) {
		super(valor, FORMATO, MENSAGEM);
	}
}
