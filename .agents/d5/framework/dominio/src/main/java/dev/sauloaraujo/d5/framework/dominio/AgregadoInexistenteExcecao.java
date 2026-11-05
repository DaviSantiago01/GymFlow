package dev.sauloaraujo.d5.framework.dominio;

import java.util.NoSuchElementException;
import java.util.Objects;

public class AgregadoInexistenteExcecao extends NoSuchElementException {
	private static final long serialVersionUID = -6427625553855957282L;

	public AgregadoInexistenteExcecao(String mensagem) {
		super(Objects.requireNonNull(mensagem));
	}
}