package com.example.exemplo.aplicacao.geografia.unidadefederativa;

import com.example.exemplo.aplicacao.geografia.unidadefederativa.spi.IUnidadeFederativaAplicacaoRepositorio;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativa;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaCodigoIbge;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaServico;

import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class UnidadeFederativaAplicacaoServico extends
		AgregadoAplicacaoServicoAbstrato<UnidadeFederativaCodigoIbge, UnidadeFederativa, AgregadoAutorizacoes<UnidadeFederativa>, UnidadeFederativaProjecao, UnidadeFederativaConsulta> {
	public UnidadeFederativaAplicacaoServico(IUnidadeFederativaAplicacaoRepositorio repositorio,
			UnidadeFederativaServico servico) {
		super(repositorio, servico);
	}
}