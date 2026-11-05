package com.example.exemplo.infraestrutura.geografia.municipio;

import java.util.Objects;
import java.util.Optional;

import com.example.exemplo.dominio.geografia.municipio.Municipio;
import com.example.exemplo.dominio.geografia.municipio.MunicipioCodigoIbge;
import com.example.exemplo.dominio.geografia.municipio.MunicipioServico;
import com.example.exemplo.dominio.geografia.municipio.spi.IMunicipioRepositorio;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaCodigoIbge;
import com.example.exemplo.infraestrutura.geografia.unidadefederativa.IUnidadeFederativaRepositorioJpa;

import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public class MunicipioRepositorio
		extends AgregadoRepositorioAbstrato<MunicipioCodigoIbge, Municipio, String, MunicipioJpa>
		implements IMunicipioRepositorio {
	private final IMunicipioRepositorioJpa repositorioJpa;
	private final IUnidadeFederativaRepositorioJpa unidadeFederativaRepositorioJpa;

	public MunicipioRepositorio(IMunicipioRepositorioJpa repositorioJpa,
			IUnidadeFederativaRepositorioJpa unidadeFederativaRepositorioJpa) {
		super(repositorioJpa, MunicipioServico.IDENTIFICADOR_INEXISTENTE_MODELO);

		this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
		this.unidadeFederativaRepositorioJpa = Objects.requireNonNull(unidadeFederativaRepositorioJpa);
	}

	@Override
	protected MunicipioJpa paraJpa(Municipio agregado) {
		Objects.requireNonNull(agregado);

		var identificador = agregado.getIdentificador().orElseThrow().toString();
		var unidadeFederativa = unidadeFederativaRepositorioJpa
				.getReferenceById(agregado.getUnidadeFederativa().toString());
		return new MunicipioJpa(identificador, agregado.getNome(), unidadeFederativa, agregado.isExtinto());
	}

	@Override
	protected Municipio paraAgregado(MunicipioJpa agregadoJpa) {
		Objects.requireNonNull(agregadoJpa);

		var identificador = new MunicipioCodigoIbge(agregadoJpa.getIdentificador());
		return new Municipio(identificador, agregadoJpa.getNome(), Optional.of(agregadoJpa.isExtinto()));
	}

	@Override
	protected String paraIdentificadorJpa(MunicipioCodigoIbge identificador) {
		Objects.requireNonNull(identificador);

		return identificador.toString();
	}

	@Override
	public boolean existePorNome(String nome, UnidadeFederativaCodigoIbge unidadeFederativa,
			Optional<MunicipioCodigoIbge> identificadorIgnorado) {
		Objects.requireNonNull(nome);
		Objects.requireNonNull(unidadeFederativa);
		Objects.requireNonNull(identificadorIgnorado);

		var identificadorIgnoradoJpa = identificadorIgnorado.map(MunicipioCodigoIbge::toString).orElse(null);
		return repositorioJpa.existePorNome(nome, unidadeFederativa.toString(), identificadorIgnoradoJpa);
	}

	@Override
	public boolean existeReferencia(MunicipioCodigoIbge identificador) {
		Objects.requireNonNull(identificador);

		return repositorioJpa.existeReferencia(identificador.toString());
	}
}