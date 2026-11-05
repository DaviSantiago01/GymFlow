package com.example.exemplo.aplicacao.crm.cliente;

import java.util.Objects;

import com.example.exemplo.dominio.crm.cliente.Cliente;
import com.example.exemplo.dominio.crm.cliente.ClienteIdentificador;
import com.example.exemplo.dominio.geografia.municipio.MunicipioCodigoIbge;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaCodigoIbge;

import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public final class ClienteProjecao implements IProjecao<Cliente> {
	private final ClienteIdentificador identificador;
	private final String nome;
	private final MunicipioCodigoIbge municipio;
	private final String municipioNome;
	private final UnidadeFederativaCodigoIbge unidadeFederativa;
	private final String unidadeFederativaNome;

	public ClienteProjecao(ClienteIdentificador identificador, String nome, MunicipioCodigoIbge municipio,
			String municipioNome, UnidadeFederativaCodigoIbge unidadeFederativa, String unidadeFederativaNome) {
		this.identificador = Objects.requireNonNull(identificador);
		this.nome = Objects.requireNonNull(nome);
		this.municipio = Objects.requireNonNull(municipio);
		this.municipioNome = Objects.requireNonNull(municipioNome);
		this.unidadeFederativa = Objects.requireNonNull(unidadeFederativa);
		this.unidadeFederativaNome = Objects.requireNonNull(unidadeFederativaNome);
	}

	public ClienteProjecao(Long identificador, String nome, String municipio, String municipioNome,
			String unidadeFederativa, String unidadeFederativaNome) {
		this(new ClienteIdentificador(identificador.toString()), nome, new MunicipioCodigoIbge(municipio),
				municipioNome, new UnidadeFederativaCodigoIbge(unidadeFederativa), unidadeFederativaNome);
	}

	public ClienteIdentificador getIdentificador() {
		return identificador;
	}

	public String getNome() {
		return nome;
	}

	public MunicipioCodigoIbge getMunicipio() {
		return municipio;
	}

	public String getMunicipioNome() {
		return municipioNome;
	}

	public UnidadeFederativaCodigoIbge getUnidadeFederativa() {
		return unidadeFederativa;
	}

	public String getUnidadeFederativaNome() {
		return unidadeFederativaNome;
	}
}
