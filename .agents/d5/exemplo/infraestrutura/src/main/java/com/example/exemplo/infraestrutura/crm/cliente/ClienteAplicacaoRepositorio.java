package com.example.exemplo.infraestrutura.crm.cliente;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.exemplo.aplicacao.crm.cliente.ClienteConsulta;
import com.example.exemplo.aplicacao.crm.cliente.ClienteOrdenacao;
import com.example.exemplo.aplicacao.crm.cliente.ClienteProjecao;
import com.example.exemplo.aplicacao.crm.cliente.spi.IClienteAplicacaoRepositorio;

import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public class ClienteAplicacaoRepositorio
		extends AgregadoAplicacaoRepositorioAbstrato<ClienteProjecao, ClienteOrdenacao, ClienteConsulta>
		implements IClienteAplicacaoRepositorio {
	private final IClienteRepositorioJpa repositorioJpa;

	public ClienteAplicacaoRepositorio(IClienteRepositorioJpa repositorioJpa) {
		this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
	}

	@Override
	protected Page<ClienteProjecao> pesquisar(ClienteConsulta consulta, Pageable pageable) {
		Objects.requireNonNull(consulta);
		Objects.requireNonNull(pageable);

		var nome = paraLike(consulta.getNome());
		var municipio = consulta.getMunicipio().orElse(null);
		var unidadeFederativa = consulta.getUnidadeFederativa().orElse(null);
		return repositorioJpa.pesquisar(nome, municipio, unidadeFederativa, pageable);
	}

	@Override
	protected String paraPropriedadeOrdenacao(ClienteOrdenacao ordenacao) {
		Objects.requireNonNull(ordenacao);

		return switch (ordenacao) {
		case IDENTIFICADOR -> "identificador";
		case NOME -> "nome";
		case MUNICIPIO_NOME -> "m.nome";
		case UNIDADE_FEDERATIVA_NOME -> "u.nome";
		};
	}
}
