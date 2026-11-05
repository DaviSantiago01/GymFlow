package com.example.exemplo.dominio.geografia.municipio;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaCodigoIbge;

import dev.sauloaraujo.d5.framework.dominio.IAgregado;

public final class Municipio implements IAgregado<MunicipioCodigoIbge> {
	public static final String EXTINTO_MODELO = "O município %s está extinto";

	private final MunicipioCodigoIbge identificador;
	private final String nome;
	private final boolean extinto;

	public Municipio(MunicipioCodigoIbge identificador, String nome, Optional<Boolean> extinto) {
		this.identificador = Objects.requireNonNull(identificador);
		this.nome = Validate.notBlank(nome, "Nome do município em branco");
		this.extinto = Objects.requireNonNull(extinto).orElse(false);
	}

	public Municipio extinguir() {
		Validate.validState(!extinto, EXTINTO_MODELO, identificador);

		return new Municipio(identificador, nome, Optional.of(true));
	}

	@Override
	public Optional<MunicipioCodigoIbge> getIdentificador() {
		return Optional.of(identificador);
	}

	public String getNome() {
		return nome;
	}

	public UnidadeFederativaCodigoIbge getUnidadeFederativa() {
		return identificador.getUnidadeFederativa();
	}

	public boolean isExtinto() {
		return extinto;
	}
}