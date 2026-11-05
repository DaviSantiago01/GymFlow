package com.example.exemplo.ui.backend.seguranca;

import java.util.Objects;

import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativa;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.ui.seguranca.AutenticacaoRecursoAbstrato;

public class AutenticacaoRecurso extends AutenticacaoRecursoAbstrato {
	private final AgregadoAutorizacoes<UnidadeFederativa> unidadeFederativa;

	public AutenticacaoRecurso(String usuario, AgregadoAutorizacoes<UnidadeFederativa> unidadeFederativa) {
		super(usuario);
		this.unidadeFederativa = Objects.requireNonNull(unidadeFederativa);
	}

	public AgregadoAutorizacoes<UnidadeFederativa> getUnidadeFederativa() {
		return unidadeFederativa;
	}
}
