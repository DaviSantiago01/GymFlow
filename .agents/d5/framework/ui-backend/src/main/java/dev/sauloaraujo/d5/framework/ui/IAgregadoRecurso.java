package dev.sauloaraujo.d5.framework.ui;

import dev.sauloaraujo.d5.framework.dominio.IAgregado;
import dev.sauloaraujo.d5.framework.dominio.IAgregadoAutorizacoes;

public interface IAgregadoRecurso<A extends IAgregado<?>, U extends IAgregadoAutorizacoes<A>> {
	A getAgregado();

	U getAutorizacoes();
}