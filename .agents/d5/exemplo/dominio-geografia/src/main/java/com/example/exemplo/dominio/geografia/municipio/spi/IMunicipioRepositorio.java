package com.example.exemplo.dominio.geografia.municipio.spi;

import java.util.Optional;

import com.example.exemplo.dominio.geografia.municipio.Municipio;
import com.example.exemplo.dominio.geografia.municipio.MunicipioCodigoIbge;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaCodigoIbge;

import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IMunicipioRepositorio extends IAgregadoRepositorio<MunicipioCodigoIbge, Municipio> {
	boolean existePorNome(String nome, UnidadeFederativaCodigoIbge unidadeFederativa,
			Optional<MunicipioCodigoIbge> identificadorIgnorado);

	boolean existeReferencia(MunicipioCodigoIbge identificador);
}