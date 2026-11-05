package dev.sauloaraujo.d5.framework.dominio.spi;

import java.util.Objects;

public class NaoAutorizadoExcecao extends RuntimeException {
	private static final long serialVersionUID = -5340214263571227790L;

	public NaoAutorizadoExcecao(String mensagem) {
		super(Objects.requireNonNull(mensagem));
	}
}