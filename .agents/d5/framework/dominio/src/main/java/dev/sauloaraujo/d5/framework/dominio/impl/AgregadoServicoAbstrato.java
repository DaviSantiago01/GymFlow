package dev.sauloaraujo.d5.framework.dominio.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import dev.sauloaraujo.d5.framework.dominio.AgregadoExistenteExcecao;
import dev.sauloaraujo.d5.framework.dominio.AgregadoInexistenteExcecao;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;
import dev.sauloaraujo.d5.framework.dominio.IAgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.dominio.IAgregadoServico;
import dev.sauloaraujo.d5.framework.dominio.IIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;

public abstract class AgregadoServicoAbstrato<I extends IIdentificador, A extends IAgregado<I>, U extends IAgregadoAutorizacoes<A>>
		implements IAgregadoServico<I, A, U> {
	private final ISegurancaServico segurancaServico;
	private final IAgregadoRepositorio<I, A> repositorio;
	private final String existenteModelo;
	private final String inexistenteModelo;

	protected AgregadoServicoAbstrato(ISegurancaServico segurancaServico, IAgregadoRepositorio<I, A> repositorio,
			String existenteModelo, String inexistenteModelo) {
		this.segurancaServico = Objects.requireNonNull(segurancaServico);
		this.repositorio = Objects.requireNonNull(repositorio);
		this.existenteModelo = Objects.requireNonNull(existenteModelo);
		this.inexistenteModelo = Objects.requireNonNull(inexistenteModelo);
	}

	protected ISegurancaServico getSegurancaServico() {
		return segurancaServico;
	}

	@Override
	public A incluir(A agregado) {
		Objects.requireNonNull(agregado);
		validarAutorizacao(Optional.empty(), IAgregadoAutorizacoes::getIncluir);
		validarInclusao(agregado);
		var identificador = agregado.getIdentificador();
		if (identificador.isPresent()) {
			validarInexistencia(identificador.get());
		}
		validarInclusaoAlteracao(agregado);
		return repositorio.incluir(agregado);
	}

	protected void validarAutorizacao(Optional<I> identificador, Predicate<U> autorizacao) {
		if (!autorizacao.test(criarAutorizacoes(identificador))) {
			throw new NaoAutorizadoExcecao("Ação não autorizada");
		}
	}

	protected void validarInexistencia(I identificador) {
		validarInexistencia(repositorio::existePorIdentificador, identificador, existenteModelo);
	}

	protected <T> void validarInexistencia(Predicate<T> predicado, T argumento, String modeloExistente) {
		Objects.requireNonNull(predicado);
		Objects.requireNonNull(modeloExistente);
		if (predicado.test(argumento)) {
			var mensagem = String.format(modeloExistente, argumento);
			throw new AgregadoExistenteExcecao(mensagem);
		}
	}

	protected void validarInclusaoAlteracao(A agregado) {
	}

	protected void validarInclusao(A agregado) {
	}

	@Override
	public A buscar(I identificador) {
		Objects.requireNonNull(identificador);
		validarAutorizacao(Optional.of(identificador), IAgregadoAutorizacoes::getBuscar);
		return repositorio.buscarPorIdentificador(identificador);
	}

	@Override
	public void alterar(A agregado) {
		Objects.requireNonNull(agregado);
		var identificador = agregado.getIdentificador().orElseThrow();
		validarAutorizacao(Optional.of(identificador), IAgregadoAutorizacoes::getAlterar);
		validarExistencia(identificador);
		validarAlteracao(agregado);
		validarInclusaoAlteracao(agregado);
		repositorio.alterar(agregado);
	}

	protected void validarExistencia(I identificador) {
		validarExistencia(repositorio::existePorIdentificador, identificador, inexistenteModelo);
	}

	protected <T> void validarExistencia(Predicate<T> predicado, T argumento, String modeloInexistente) {
		Objects.requireNonNull(predicado);
		Objects.requireNonNull(modeloInexistente);
		if (!predicado.test(argumento)) {
			var mensagem = String.format(modeloInexistente, argumento);
			throw new AgregadoInexistenteExcecao(mensagem);
		}
	}

	protected void validarAlteracao(A agregado) {
	}

	@Override
	public void excluir(I identificador) {
		Objects.requireNonNull(identificador);
		validarAutorizacao(Optional.of(identificador), IAgregadoAutorizacoes::getExcluir);
		validarExistencia(identificador);
		validarExclusao(identificador);
		repositorio.excluir(identificador);
	}

	protected void validarExclusao(I identificador) {
	}
}
