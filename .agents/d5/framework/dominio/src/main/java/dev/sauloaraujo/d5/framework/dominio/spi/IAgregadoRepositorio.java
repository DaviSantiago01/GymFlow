package dev.sauloaraujo.d5.framework.dominio.spi;

import dev.sauloaraujo.d5.framework.dominio.AgregadoInexistenteExcecao;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;
import dev.sauloaraujo.d5.framework.dominio.IIdentificador;

public interface IAgregadoRepositorio<I extends IIdentificador, A extends IAgregado<I>> {
	A incluir(A agregado);

	boolean existePorIdentificador(I identificador);

	A buscarPorIdentificador(I identificador) throws AgregadoInexistenteExcecao;

	void alterar(A agregado);

	void excluir(I identificador);
}