package com.example.exemplo.infraestrutura.geografia.unidadefederativa;

import java.util.Objects;
import java.util.Optional;

import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativa;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaCodigoIbge;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaServico;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaSigla;
import com.example.exemplo.dominio.geografia.unidadefederativa.spi.IUnidadeFederativaRepositorio;

import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public class UnidadeFederativaRepositorio extends
		AgregadoRepositorioAbstrato<UnidadeFederativaCodigoIbge, UnidadeFederativa, String, UnidadeFederativaJpa>
		implements IUnidadeFederativaRepositorio {
	private final IUnidadeFederativaRepositorioJpa repositorioJpa;

	public UnidadeFederativaRepositorio(IUnidadeFederativaRepositorioJpa repositorioJpa) {
		super(repositorioJpa, UnidadeFederativaServico.IDENTIFICADOR_INEXISTENTE_MODELO);

		this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
	}

	@Override
	protected UnidadeFederativaJpa paraJpa(UnidadeFederativa agregado) {
		Objects.requireNonNull(agregado);

		var identificador = agregado.getIdentificador().orElseThrow().toString();
		return new UnidadeFederativaJpa(identificador, agregado.getSigla().toString(), agregado.getNome());
	}

	@Override
	protected UnidadeFederativa paraAgregado(UnidadeFederativaJpa agregadoJpa) {
		Objects.requireNonNull(agregadoJpa);

		return new UnidadeFederativa(new UnidadeFederativaCodigoIbge(agregadoJpa.getIdentificador()),
				new UnidadeFederativaSigla(agregadoJpa.getSigla()), agregadoJpa.getNome());
	}

	@Override
	protected String paraIdentificadorJpa(UnidadeFederativaCodigoIbge identificador) {
		Objects.requireNonNull(identificador);

		return identificador.toString();
	}

	@Override
	public boolean existePorSigla(UnidadeFederativaSigla sigla,
			Optional<UnidadeFederativaCodigoIbge> identificadorIgnorado) {
		Objects.requireNonNull(sigla);
		Objects.requireNonNull(identificadorIgnorado);

		var identificadorIgnoradoJpa = identificadorIgnorado.map(UnidadeFederativaCodigoIbge::toString).orElse(null);
		return repositorioJpa.existePorSigla(sigla.toString(), identificadorIgnoradoJpa);
	}

	@Override
	public boolean existePorNome(String nome, Optional<UnidadeFederativaCodigoIbge> identificadorIgnorado) {
		Objects.requireNonNull(nome);
		Objects.requireNonNull(identificadorIgnorado);

		var identificadorIgnoradoJpa = identificadorIgnorado.map(UnidadeFederativaCodigoIbge::toString).orElse(null);
		return repositorioJpa.existePorNome(nome, identificadorIgnoradoJpa);
	}

	@Override
	public boolean existeReferencia(UnidadeFederativaCodigoIbge identificador) {
		Objects.requireNonNull(identificador);

		return repositorioJpa.existeReferencia(identificador.toString());
	}
}