package dev.sauloaraujo.d5.framework.dominio.impl;

import dev.sauloaraujo.d5.framework.dominio.IAgregado;
import dev.sauloaraujo.d5.framework.dominio.IAgregadoAutorizacoes;

public class AgregadoAutorizacoes<A extends IAgregado<?>> implements IAgregadoAutorizacoes<A> {
	private final boolean incluir;
	private final boolean buscar;
	private final boolean alterar;
	private final boolean excluir;

	public AgregadoAutorizacoes(boolean incluir, boolean buscar, boolean alterar, boolean excluir) {
		this.incluir = incluir;
		this.buscar = buscar;
		this.alterar = alterar;
		this.excluir = excluir;
	}

	@Override
	public boolean getIncluir() {
		return incluir;
	}

	@Override
	public boolean getBuscar() {
		return buscar;
	}

	@Override
	public boolean getAlterar() {
		return alterar;
	}

	@Override
	public boolean getExcluir() {
		return excluir;
	}
}