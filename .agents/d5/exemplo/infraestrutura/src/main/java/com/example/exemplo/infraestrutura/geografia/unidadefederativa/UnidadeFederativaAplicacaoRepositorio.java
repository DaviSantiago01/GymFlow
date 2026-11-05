package com.example.exemplo.infraestrutura.geografia.unidadefederativa;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.exemplo.aplicacao.geografia.unidadefederativa.UnidadeFederativaConsulta;
import com.example.exemplo.aplicacao.geografia.unidadefederativa.UnidadeFederativaOrdenacao;
import com.example.exemplo.aplicacao.geografia.unidadefederativa.UnidadeFederativaProjecao;
import com.example.exemplo.aplicacao.geografia.unidadefederativa.spi.IUnidadeFederativaAplicacaoRepositorio;

import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public class UnidadeFederativaAplicacaoRepositorio extends
		AgregadoAplicacaoRepositorioAbstrato<UnidadeFederativaProjecao, UnidadeFederativaOrdenacao, UnidadeFederativaConsulta>
		implements IUnidadeFederativaAplicacaoRepositorio {
	private final IUnidadeFederativaRepositorioJpa repositorioJpa;

	public UnidadeFederativaAplicacaoRepositorio(IUnidadeFederativaRepositorioJpa repositorioJpa) {
		this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
	}

	@Override
	protected Page<UnidadeFederativaProjecao> pesquisar(UnidadeFederativaConsulta consulta, Pageable pageable) {
		Objects.requireNonNull(consulta);
		Objects.requireNonNull(pageable);

		var sigla = paraLike(consulta.getSigla());
		var nome = paraLike(consulta.getNome());
		return repositorioJpa.pesquisar(sigla, nome, pageable);
	}

	@Override
	protected String paraPropriedadeOrdenacao(UnidadeFederativaOrdenacao ordenacao) {
		Objects.requireNonNull(ordenacao);

		return switch (ordenacao) {
		case IDENTIFICADOR -> "identificador";
		case SIGLA -> "sigla";
		case NOME -> "nome";
		};
	}
}