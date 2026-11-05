package com.example.exemplo.aplicacao.geografia.unidadefederativa.spi;

import com.example.exemplo.aplicacao.geografia.unidadefederativa.UnidadeFederativaConsulta;
import com.example.exemplo.aplicacao.geografia.unidadefederativa.UnidadeFederativaProjecao;

import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IUnidadeFederativaAplicacaoRepositorio
		extends IAgregadoAplicacaoRepositorio<UnidadeFederativaProjecao, UnidadeFederativaConsulta> {
}