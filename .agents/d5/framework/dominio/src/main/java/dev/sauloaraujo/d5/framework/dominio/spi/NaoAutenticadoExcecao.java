package dev.sauloaraujo.d5.framework.dominio.spi;

import java.util.Objects;

public class NaoAutenticadoExcecao extends RuntimeException {
	private static final long serialVersionUID = 8201692097715103859L;

	public NaoAutenticadoExcecao(String mensagem) {
		super(Objects.requireNonNull(mensagem));
	}
}