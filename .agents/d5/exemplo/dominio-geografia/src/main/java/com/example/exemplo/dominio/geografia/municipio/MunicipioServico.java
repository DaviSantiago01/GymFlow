package com.example.exemplo.dominio.geografia.municipio;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import com.example.exemplo.dominio.Autorizacao;
import com.example.exemplo.dominio.geografia.municipio.spi.IMunicipioRepositorio;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaServico;
import com.example.exemplo.dominio.geografia.unidadefederativa.spi.IUnidadeFederativaRepositorio;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

public final class MunicipioServico
		extends AgregadoServicoAbstrato<MunicipioCodigoIbge, Municipio, MunicipioAutorizacoes> {
	public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe município com o identificador %s";
	public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe um município com o identificador %s";
	public static final String NOME_EXISTENTE_MODELO = "Já existe um município com o nome %s na unidade federativa";
	public static final String EXTINCAO_INDEVIDA = "O município só pode ser extinto pela ação de extinção";
	public static final String REFERENCIADO_MODELO = "O município %s é referenciado e não pode ser excluído";

	private final IMunicipioRepositorio repositorio;
	private final IUnidadeFederativaRepositorio unidadeFederativaRepositorio;

	public MunicipioServico(ISegurancaServico segurancaServico, IMunicipioRepositorio repositorio,
			IUnidadeFederativaRepositorio unidadeFederativaRepositorio) {
		super(segurancaServico, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);

		this.repositorio = Objects.requireNonNull(repositorio);
		this.unidadeFederativaRepositorio = Objects.requireNonNull(unidadeFederativaRepositorio);
	}

	@Override
	public MunicipioAutorizacoes criarAutorizacoes(Optional<MunicipioCodigoIbge> identificador) {
		Objects.requireNonNull(identificador);

		var administrador = getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name());
		return new MunicipioAutorizacoes(administrador, true, administrador, administrador, administrador);
	}

	@Override
	protected void validarInclusao(Municipio agregado) {
		Objects.requireNonNull(agregado);

		Validate.isTrue(!agregado.isExtinto(), EXTINCAO_INDEVIDA);
	}

	@Override
	protected void validarInclusaoAlteracao(Municipio agregado) {
		Objects.requireNonNull(agregado);

		validarExistencia(unidadeFederativaRepositorio::existePorIdentificador, agregado.getUnidadeFederativa(),
				UnidadeFederativaServico.IDENTIFICADOR_INEXISTENTE_MODELO);

		validarInexistencia(
				nome -> repositorio.existePorNome(nome, agregado.getUnidadeFederativa(), agregado.getIdentificador()),
				agregado.getNome(), NOME_EXISTENTE_MODELO);
	}

	@Override
	protected void validarAlteracao(Municipio agregado) {
		Objects.requireNonNull(agregado);

		Validate.isTrue(!agregado.isExtinto(), EXTINCAO_INDEVIDA);

		var identificador = agregado.getIdentificador().orElseThrow();
		var municipio = repositorio.buscarPorIdentificador(identificador);
		Validate.validState(!municipio.isExtinto(), Municipio.EXTINTO_MODELO, identificador);
	}

	@Override
	protected void validarExclusao(MunicipioCodigoIbge identificador) {
		Objects.requireNonNull(identificador);

		Validate.validState(!repositorio.existeReferencia(identificador), REFERENCIADO_MODELO, identificador);
	}

	public void extinguir(MunicipioCodigoIbge identificador) {
		Objects.requireNonNull(identificador);

		validarAutorizacao(Optional.of(identificador), MunicipioAutorizacoes::getExtinguir);
		validarExistencia(identificador);

		var municipio = repositorio.buscarPorIdentificador(identificador);
		repositorio.alterar(municipio.extinguir());
	}
}
