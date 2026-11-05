package dev.sauloaraujo.d5.framework.ui.seguranca;

import java.util.Objects;

public abstract class AutenticacaoRecursoAbstrato {
	private final String usuario;

	protected AutenticacaoRecursoAbstrato(String usuario) {
		this.usuario = Objects.requireNonNull(usuario);
	}

	public String getUsuario() {
		return usuario;
	}
}
