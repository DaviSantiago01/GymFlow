package dev.sauloaraujo.d5.framework.ui;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import dev.sauloaraujo.d5.framework.aplicacao.IAgregadoAplicacaoServico;
import dev.sauloaraujo.d5.framework.aplicacao.IConsulta;
import dev.sauloaraujo.d5.framework.aplicacao.IConsultaResultado;
import dev.sauloaraujo.d5.framework.aplicacao.IOrdenacao;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;
import dev.sauloaraujo.d5.framework.dominio.IAgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.dominio.IAgregadoServico;
import dev.sauloaraujo.d5.framework.dominio.IIdentificador;

public abstract class AgregadoControladorAbstrato<I extends IIdentificador, A extends IAgregado<I>, U extends IAgregadoAutorizacoes<A>, P extends IProjecao<A>, C extends IConsulta<? extends IOrdenacao<P>>> {
	private final IAgregadoServico<I, A, U> servico;
	private final IAgregadoAplicacaoServico<P, C> servicoAplicacao;

	protected AgregadoControladorAbstrato(IAgregadoServico<I, A, U> servico,
			IAgregadoAplicacaoServico<P, C> servicoAplicacao) {
		this.servico = Objects.requireNonNull(servico);
		this.servicoAplicacao = Objects.requireNonNull(servicoAplicacao);
	}

	@GetMapping("inclusao")
	public Object inclusao() {
		return new Object();
	}

	@PostMapping("incluir")
	public void incluir(@RequestBody A agregado) {
		Objects.requireNonNull(agregado);
		servico.incluir(agregado);
	}

	@GetMapping("{identificador}/alteracao")
	public IAgregadoRecurso<A, U> alteracao(@PathVariable I identificador) {
		Objects.requireNonNull(identificador);
		var agregado = servico.buscar(identificador);
		U autorizacoes = autorizacoes(Optional.of(identificador));
		return alteracao(agregado, autorizacoes);
	}

	protected U autorizacoes(Optional<I> identificador) {
		return servico.criarAutorizacoes(identificador);
	}

	protected IAgregadoRecurso<A, U> alteracao(A agregado, U autorizacoes) {
		Objects.requireNonNull(agregado);
		return new AgregadoRecurso<>(agregado, autorizacoes);
	}

	@PostMapping("{identificador}/alterar")
	public void alterar(@PathVariable I identificador, @RequestBody A agregado) {
		Objects.requireNonNull(identificador);
		Objects.requireNonNull(agregado);
		Validate.isTrue(Objects.equals(Optional.of(identificador), agregado.getIdentificador()),
				"O identificador do caminho difere do identificador do agregado");
		servico.alterar(agregado);
	}

	@GetMapping("{identificador}/exclusao")
	public IAgregadoRecurso<A, U> exclusao(@PathVariable I identificador) {
		Objects.requireNonNull(identificador);
		var agregado = servico.buscar(identificador);
		U autorizacoes = autorizacoes(Optional.of(identificador));
		return exclusao(agregado, autorizacoes);
	}

	protected IAgregadoRecurso<A, U> exclusao(A agregado, U autorizacoes) {
		return alteracao(agregado, autorizacoes);
	}

	@PostMapping("{identificador}/excluir")
	public void excluir(@PathVariable I identificador) {
		Objects.requireNonNull(identificador);
		servico.excluir(identificador);
	}

	@GetMapping("pesquisa")
	public IPesquisaRecurso<P, C> pesquisa(@ModelAttribute C consulta) {
		Objects.requireNonNull(consulta);
		var resultado = servicoAplicacao.pesquisar(consulta);
		return pesquisa(consulta, resultado);
	}

	protected IPesquisaRecurso<P, C> pesquisa(C consulta, IConsultaResultado<P> resultado) {
		return new PesquisaRecurso<>(consulta, resultado);
	}
}