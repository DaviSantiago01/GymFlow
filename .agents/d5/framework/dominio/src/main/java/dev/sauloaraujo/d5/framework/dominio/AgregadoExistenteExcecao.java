package dev.sauloaraujo.d5.framework.dominio;

import java.util.Objects;

public class AgregadoExistenteExcecao extends IllegalStateException {
	private static final long serialVersionUID = 5561053670486841675L;

	public AgregadoExistenteExcecao(String mensagem) {
		super(Objects.requireNonNull(mensagem));
	}
}