package com.example.exemplo.infraestrutura.crm.cliente;

import org.jspecify.annotations.Nullable;

import com.example.exemplo.dominio.crm.cliente.Cliente;
import com.example.exemplo.dominio.crm.cliente.Cpf;
import com.example.exemplo.dominio.crm.cliente.EnderecoCep;
import com.example.exemplo.infraestrutura.geografia.municipio.MunicipioJpa;

import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "CLI_CLIENTE")
public class ClienteJpa implements IAgregadoJpa<Cliente> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CLI_ID", updatable = false)
	private @Nullable Long identificador;

	@Column(name = "CLI_CPF", unique = true)
	@NotNull
	@Size(max = Cpf.TAMANHO)
	private String cpf;

	@Column(name = "CLI_NM")
	@NotNull
	@Size(max = 100)
	private String nome;

	@Column(name = "CLI_LOGRADOURO")
	@NotNull
	@Size(max = 100)
	private String logradouro;

	@Column(name = "CLI_NUMERO")
	@NotNull
	@Size(max = 10)
	private String numero;

	@Column(name = "CLI_COMPLEMENTO")
	@Size(max = 50)
	private @Nullable String complemento;

	@Column(name = "CLI_BAIRRO")
	@NotNull
	@Size(max = 50)
	private String bairro;

	@Column(name = "CLI_CEP")
	@NotNull
	@Size(max = EnderecoCep.TAMANHO)
	private String cep;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MUN_ID")
	@NotNull
	private MunicipioJpa municipio;

	protected ClienteJpa() {
	}

	public ClienteJpa(@Nullable Long identificador, String cpf, String nome, String logradouro, String numero,
			@Nullable String complemento, String bairro, String cep, MunicipioJpa municipio) {
		this.identificador = identificador;
		this.cpf = cpf;
		this.nome = nome;
		this.logradouro = logradouro;
		this.numero = numero;
		this.complemento = complemento;
		this.bairro = bairro;
		this.cep = cep;
		this.municipio = municipio;
	}

	public @Nullable Long getIdentificador() {
		return identificador;
	}

	public String getCpf() {
		return cpf;
	}

	public String getNome() {
		return nome;
	}

	public String getLogradouro() {
		return logradouro;
	}

	public String getNumero() {
		return numero;
	}

	public @Nullable String getComplemento() {
		return complemento;
	}

	public String getBairro() {
		return bairro;
	}

	public String getCep() {
		return cep;
	}

	public MunicipioJpa getMunicipio() {
		return municipio;
	}
}