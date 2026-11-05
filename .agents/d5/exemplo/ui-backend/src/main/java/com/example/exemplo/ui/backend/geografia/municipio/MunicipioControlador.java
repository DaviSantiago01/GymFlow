package com.example.exemplo.ui.backend.geografia.municipio;

import java.util.Objects;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.exemplo.aplicacao.geografia.municipio.MunicipioAplicacaoServico;
import com.example.exemplo.aplicacao.geografia.municipio.MunicipioConsulta;
import com.example.exemplo.aplicacao.geografia.municipio.MunicipioProjecao;
import com.example.exemplo.dominio.geografia.municipio.Municipio;
import com.example.exemplo.dominio.geografia.municipio.MunicipioAutorizacoes;
import com.example.exemplo.dominio.geografia.municipio.MunicipioCodigoIbge;
import com.example.exemplo.dominio.geografia.municipio.MunicipioServico;

import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "geografia/municipio")
public class MunicipioControlador extends
		AgregadoControladorAbstrato<MunicipioCodigoIbge, Municipio, MunicipioAutorizacoes, MunicipioProjecao, MunicipioConsulta> {
	private final MunicipioServico servico;

	public MunicipioControlador(MunicipioServico servico, MunicipioAplicacaoServico servicoAplicacao) {
		super(servico, servicoAplicacao);

		this.servico = Objects.requireNonNull(servico);
	}

	@PostMapping("{identificador}/extinguir")
	public void extinguir(@PathVariable MunicipioCodigoIbge identificador) {
		Objects.requireNonNull(identificador);

		servico.extinguir(identificador);
	}
}