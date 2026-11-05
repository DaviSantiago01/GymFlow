package dev.sauloaraujo.d5.framework.dominio.impl;

import org.apache.commons.lang3.Validate;

public abstract class ObjetoValorRegexAbstrato extends ObjetoValorStringAbstrato {
	protected ObjetoValorRegexAbstrato(String valor, String formato, String mensagem) {
		super(valor);
		Validate.matchesPattern(valor, formato, mensagem, valor);
	}
}