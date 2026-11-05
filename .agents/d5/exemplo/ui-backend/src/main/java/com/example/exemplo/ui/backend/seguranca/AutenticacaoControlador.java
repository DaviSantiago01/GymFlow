package com.example.exemplo.ui.backend.seguranca;

import java.util.Objects;
import java.util.Optional;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaServico;

import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.ui.Backend;
import dev.sauloaraujo.d5.framework.ui.seguranca.AutenticacaoControladorAbstrato;

@RestController
@RequestMapping(Backend.PREFIXO + "autenticacao")
public class AutenticacaoControlador extends AutenticacaoControladorAbstrato {
	private final UnidadeFederativaServico unidadeFederativaServico;

	public AutenticacaoControlador(ISegurancaServico segurancaServico,
			UnidadeFederativaServico unidadeFederativaServico) {
		super(segurancaServico);
		this.unidadeFederativaServico = Objects.requireNonNull(unidadeFederativaServico);
	}

	@Override
	protected AutenticacaoRecurso recurso() {
		var unidadeFederativa = unidadeFederativaServico.criarAutorizacoes(Optional.empty());
		return new AutenticacaoRecurso(segurancaServico.getUsuario(), unidadeFederativa);
	}
}
