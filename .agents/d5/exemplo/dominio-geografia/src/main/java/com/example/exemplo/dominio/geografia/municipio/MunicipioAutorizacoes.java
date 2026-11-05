package com.example.exemplo.dominio.geografia.municipio;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class MunicipioAutorizacoes extends AgregadoAutorizacoes<Municipio> {
	private final boolean extinguir;

	public MunicipioAutorizacoes(boolean incluir, boolean buscar, boolean alterar, boolean excluir, boolean extinguir) {
		super(incluir, buscar, alterar, excluir);

		this.extinguir = extinguir;
	}

	public boolean getExtinguir() {
		return extinguir;
	}
}