package dev.sauloaraujo.d5.framework.dominio;

import java.util.Optional;

public interface IAgregadoServico<I extends IIdentificador, A extends IAgregado<I>, U extends IAgregadoAutorizacoes<A>> {
	A incluir(A agregado);

	A buscar(I identificador);

	void alterar(A agregado);

	void excluir(I identificador);

	U criarAutorizacoes(Optional<I> identificador);
}