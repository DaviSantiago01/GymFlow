package com.example.exemplo.dominio.crm.cliente;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import dev.sauloaraujo.d5.framework.dominio.IAgregado;

public final class Cliente implements IAgregado<ClienteIdentificador> {
	private final Optional<ClienteIdentificador> identificador;
	private final Cpf cpf;
	private final String nome;
	private final Endereco endereco;

	public Cliente(Optional<ClienteIdentificador> identificador, Cpf cpf, String nome, Endereco endereco) {
		this.identificador = Objects.requireNonNull(identificador);
		this.cpf = Objects.requireNonNull(cpf);
		this.nome = Validate.notBlank(nome, "Nome do cliente em branco");
		this.endereco = Objects.requireNonNull(endereco);
	}

	@Override
	public Optional<ClienteIdentificador> getIdentificador() {
		return identificador;
	}

	public Cpf getCpf() {
		return cpf;
	}

	public String getNome() {
		return nome;
	}

	public Endereco getEndereco() {
		return endereco;
	}
}
