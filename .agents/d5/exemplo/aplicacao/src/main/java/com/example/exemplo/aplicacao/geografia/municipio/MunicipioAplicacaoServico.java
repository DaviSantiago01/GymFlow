package com.example.exemplo.aplicacao.geografia.municipio;

import com.example.exemplo.aplicacao.geografia.municipio.spi.IMunicipioAplicacaoRepositorio;
import com.example.exemplo.dominio.geografia.municipio.Municipio;
import com.example.exemplo.dominio.geografia.municipio.MunicipioAutorizacoes;
import com.example.exemplo.dominio.geografia.municipio.MunicipioCodigoIbge;
import com.example.exemplo.dominio.geografia.municipio.MunicipioServico;

import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;

public final class MunicipioAplicacaoServico extends
		AgregadoAplicacaoServicoAbstrato<MunicipioCodigoIbge, Municipio, MunicipioAutorizacoes, MunicipioProjecao, MunicipioConsulta> {
	public MunicipioAplicacaoServico(IMunicipioAplicacaoRepositorio repositorio, MunicipioServico servico) {
		super(repositorio, servico);
	}
}