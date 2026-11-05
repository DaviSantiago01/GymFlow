package com.example.exemplo.dominio.geografia.unidadefederativa.spi;

import java.util.Optional;

import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativa;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaCodigoIbge;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaSigla;

import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IUnidadeFederativaRepositorio
		extends IAgregadoRepositorio<UnidadeFederativaCodigoIbge, UnidadeFederativa> {
	boolean existePorSigla(UnidadeFederativaSigla sigla, Optional<UnidadeFederativaCodigoIbge> identificadorIgnorado);

	boolean existePorNome(String nome, Optional<UnidadeFederativaCodigoIbge> identificadorIgnorado);

	boolean existeReferencia(UnidadeFederativaCodigoIbge identificador);
}