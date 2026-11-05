package dev.sauloaraujo.d5.framework.dominio;

public interface IAgregadoAutorizacoes<A extends IAgregado<?>> {
	boolean getIncluir();

	boolean getBuscar();

	boolean getAlterar();

	boolean getExcluir();
}