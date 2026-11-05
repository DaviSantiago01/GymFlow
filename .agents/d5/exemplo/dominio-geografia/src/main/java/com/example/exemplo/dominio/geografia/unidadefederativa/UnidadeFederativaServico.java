package com.example.exemplo.dominio.geografia.unidadefederativa;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import com.example.exemplo.dominio.Autorizacao;
import com.example.exemplo.dominio.geografia.unidadefederativa.spi.IUnidadeFederativaRepositorio;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

public final class UnidadeFederativaServico extends
		AgregadoServicoAbstrato<UnidadeFederativaCodigoIbge, UnidadeFederativa, AgregadoAutorizacoes<UnidadeFederativa>> {
	public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe unidade federativa com o identificador %s";
	public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe uma unidade federativa com o identificador %s";
	public static final String SIGLA_EXISTENTE_MODELO = "Já existe uma unidade federativa com a sigla %s";
	public static final String NOME_EXISTENTE_MODELO = "Já existe uma unidade federativa com o nome %s";
	public static final String REFERENCIADA_MODELO = "A unidade federativa %s é referenciada e não pode ser excluída";

	private final IUnidadeFederativaRepositorio repositorio;

	public UnidadeFederativaServico(ISegurancaServico segurancaServico, IUnidadeFederativaRepositorio repositorio) {
		super(segurancaServico, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);

		this.repositorio = Objects.requireNonNull(repositorio);
	}

	@Override
	public AgregadoAutorizacoes<UnidadeFederativa> criarAutorizacoes(
			Optional<UnidadeFederativaCodigoIbge> identificador) {
		Objects.requireNonNull(identificador);

		var administrador = getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name());
		return new AgregadoAutorizacoes<>(administrador, true, administrador, administrador);
	}

	@Override
	protected void validarInclusaoAlteracao(UnidadeFederativa agregado) {
		Objects.requireNonNull(agregado);

		var identificador = agregado.getIdentificador();

		validarInexistencia(sigla -> repositorio.existePorSigla(sigla, identificador), agregado.getSigla(),
				SIGLA_EXISTENTE_MODELO);

		validarInexistencia(nome -> repositorio.existePorNome(nome, identificador), agregado.getNome(),
				NOME_EXISTENTE_MODELO);
	}

	@Override
	protected void validarExclusao(UnidadeFederativaCodigoIbge identificador) {
		Objects.requireNonNull(identificador);

		Validate.validState(!repositorio.existeReferencia(identificador), REFERENCIADA_MODELO, identificador);
	}
}
