package com.example.exemplo.infraestrutura.geografia.municipio;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.exemplo.aplicacao.geografia.municipio.MunicipioConsulta;
import com.example.exemplo.aplicacao.geografia.municipio.MunicipioOrdenacao;
import com.example.exemplo.aplicacao.geografia.municipio.MunicipioProjecao;
import com.example.exemplo.aplicacao.geografia.municipio.spi.IMunicipioAplicacaoRepositorio;

import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public class MunicipioAplicacaoRepositorio
		extends AgregadoAplicacaoRepositorioAbstrato<MunicipioProjecao, MunicipioOrdenacao, MunicipioConsulta>
		implements IMunicipioAplicacaoRepositorio {
	private final IMunicipioRepositorioJpa repositorioJpa;

	public MunicipioAplicacaoRepositorio(IMunicipioRepositorioJpa repositorioJpa) {
		this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
	}

	@Override
	protected Page<MunicipioProjecao> pesquisar(MunicipioConsulta consulta, Pageable pageable) {
		Objects.requireNonNull(consulta);
		Objects.requireNonNull(pageable);

		var nome = paraLike(consulta.getNome());
		var unidadeFederativa = consulta.getUnidadeFederativa().orElse(null);
		return repositorioJpa.pesquisar(nome, unidadeFederativa, pageable);
	}

	@Override
	protected String paraPropriedadeOrdenacao(MunicipioOrdenacao ordenacao) {
		Objects.requireNonNull(ordenacao);

		return switch (ordenacao) {
		case IDENTIFICADOR -> "identificador";
		case NOME -> "nome";
		case UNIDADE_FEDERATIVA_NOME -> "u.nome";
		};
	}
}