package com.example.exemplo.ui.backend.geografia.unidadefederativa;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.exemplo.aplicacao.geografia.unidadefederativa.UnidadeFederativaAplicacaoServico;
import com.example.exemplo.aplicacao.geografia.unidadefederativa.UnidadeFederativaConsulta;
import com.example.exemplo.aplicacao.geografia.unidadefederativa.UnidadeFederativaProjecao;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativa;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaCodigoIbge;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaServico;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "geografia/unidade-federativa")
public class UnidadeFederativaControlador extends
		AgregadoControladorAbstrato<UnidadeFederativaCodigoIbge, UnidadeFederativa, AgregadoAutorizacoes<UnidadeFederativa>, UnidadeFederativaProjecao, UnidadeFederativaConsulta> {
	public UnidadeFederativaControlador(UnidadeFederativaServico servico,
			UnidadeFederativaAplicacaoServico servicoAplicacao) {
		super(servico, servicoAplicacao);
	}
}