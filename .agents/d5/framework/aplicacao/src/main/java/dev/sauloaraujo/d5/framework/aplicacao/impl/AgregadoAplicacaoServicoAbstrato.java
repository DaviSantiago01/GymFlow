package dev.sauloaraujo.d5.framework.aplicacao.impl;

import java.util.Objects;
import java.util.Optional;

import dev.sauloaraujo.d5.framework.aplicacao.IAgregadoAplicacaoServico;
import dev.sauloaraujo.d5.framework.aplicacao.IConsulta;
import dev.sauloaraujo.d5.framework.aplicacao.IConsultaResultado;
import dev.sauloaraujo.d5.framework.aplicacao.IOrdenacao;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;
import dev.sauloaraujo.d5.framework.dominio.IAgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.dominio.IAgregadoServico;
import dev.sauloaraujo.d5.framework.dominio.IIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;

public abstract class AgregadoAplicacaoServicoAbstrato<I extends IIdentificador, A extends IAgregado<I>, U extends IAgregadoAutorizacoes<A>, P extends IProjecao<A>, C extends IConsulta<? extends IOrdenacao<P>>>
		implements IAgregadoAplicacaoServico<P, C> {
	private final IAgregadoAplicacaoRepositorio<P, C> repositorio;
	private final IAgregadoServico<I, A, U> servico;

	protected AgregadoAplicacaoServicoAbstrato(IAgregadoAplicacaoRepositorio<P, C> repositorio,
			IAgregadoServico<I, A, U> servico) {
		this.repositorio = Objects.requireNonNull(repositorio);
		this.servico = Objects.requireNonNull(servico);
	}

	@Override
	public IConsultaResultado<P> pesquisar(C consulta) {
		Objects.requireNonNull(consulta);
		if (!servico.criarAutorizacoes(Optional.empty()).getBuscar()) {
			throw new NaoAutorizadoExcecao("Ação não autorizada");
		}
		return repositorio.pesquisar(consulta);
	}
}
