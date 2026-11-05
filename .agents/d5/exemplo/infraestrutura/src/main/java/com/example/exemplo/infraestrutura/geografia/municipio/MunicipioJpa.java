package com.example.exemplo.infraestrutura.geografia.municipio;

import com.example.exemplo.dominio.geografia.municipio.Municipio;
import com.example.exemplo.dominio.geografia.municipio.MunicipioCodigoIbge;
import com.example.exemplo.infraestrutura.geografia.unidadefederativa.UnidadeFederativaJpa;

import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "MUN_MUNICIPIO", uniqueConstraints = @UniqueConstraint(columnNames = { "UFI_ID", "MUN_NM" }))
public class MunicipioJpa implements IAgregadoJpa<Municipio> {
	@Id
	@Column(name = "MUN_ID", updatable = false)
	@NotNull
	@Size(max = MunicipioCodigoIbge.TAMANHO)
	private String identificador;

	@Column(name = "MUN_NM")
	@NotNull
	@Size(max = 100)
	private String nome;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "UFI_ID", updatable = false)
	@NotNull
	private UnidadeFederativaJpa unidadeFederativa;

	@Column(name = "MUN_EX")
	@NotNull
	private boolean extinto;

	protected MunicipioJpa() {
	}

	public MunicipioJpa(String identificador, String nome, UnidadeFederativaJpa unidadeFederativa, boolean extinto) {
		this.identificador = identificador;
		this.nome = nome;
		this.unidadeFederativa = unidadeFederativa;
		this.extinto = extinto;
	}

	public String getIdentificador() {
		return identificador;
	}

	public String getNome() {
		return nome;
	}

	public UnidadeFederativaJpa getUnidadeFederativa() {
		return unidadeFederativa;
	}

	public boolean isExtinto() {
		return extinto;
	}
}