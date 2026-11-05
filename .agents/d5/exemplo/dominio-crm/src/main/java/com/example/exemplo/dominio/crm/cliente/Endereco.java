package com.example.exemplo.dominio.crm.cliente;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.jspecify.annotations.Nullable;

import com.example.exemplo.dominio.geografia.municipio.MunicipioCodigoIbge;

import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

public final class Endereco implements IObjetoValor {
	private final String logradouro;
	private final String numero;
	private final Optional<String> complemento;
	private final String bairro;
	private final EnderecoCep cep;
	private final MunicipioCodigoIbge municipio;

	public Endereco(String logradouro, String numero, Optional<String> complemento, String bairro, EnderecoCep cep,
			MunicipioCodigoIbge municipio) {
		this.logradouro = Validate.notBlank(logradouro, "Logradouro do endereço em branco");
		this.numero = Validate.notBlank(numero, "Número do endereço em branco");
		this.complemento = Objects.requireNonNull(complemento);
		this.bairro = Validate.notBlank(bairro, "Bairro do endereço em branco");
		this.cep = Objects.requireNonNull(cep);
		this.municipio = Objects.requireNonNull(municipio);
	}

	public String getLogradouro() {
		return logradouro;
	}

	public String getNumero() {
		return numero;
	}

	public Optional<String> getComplemento() {
		return complemento;
	}

	public String getBairro() {
		return bairro;
	}

	public EnderecoCep getCep() {
		return cep;
	}

	public MunicipioCodigoIbge getMunicipio() {
		return municipio;
	}

	@Override
	public boolean equals(@Nullable Object objeto) {
		if (this == objeto) {
			return true;
		}
		if (objeto == null || getClass() != objeto.getClass()) {
			return false;
		}
		var outro = (Endereco) objeto;
		return logradouro.equals(outro.logradouro) && numero.equals(outro.numero)
				&& complemento.equals(outro.complemento) && bairro.equals(outro.bairro) && cep.equals(outro.cep)
				&& municipio.equals(outro.municipio);
	}

	@Override
	public int hashCode() {
		return Objects.hash(logradouro, numero, complemento, bairro, cep, municipio);
	}
}
