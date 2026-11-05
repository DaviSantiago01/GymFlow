package com.example.exemplo.aplicacao.geografia.municipio.spi;

import com.example.exemplo.aplicacao.geografia.municipio.MunicipioConsulta;
import com.example.exemplo.aplicacao.geografia.municipio.MunicipioProjecao;

import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IMunicipioAplicacaoRepositorio
		extends IAgregadoAplicacaoRepositorio<MunicipioProjecao, MunicipioConsulta> {
}