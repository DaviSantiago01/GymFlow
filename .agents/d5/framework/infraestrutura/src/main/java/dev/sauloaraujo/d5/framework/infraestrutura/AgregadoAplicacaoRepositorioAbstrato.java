package dev.sauloaraujo.d5.framework.infraestrutura;

import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.EscapeCharacter;

import dev.sauloaraujo.d5.framework.aplicacao.IConsulta;
import dev.sauloaraujo.d5.framework.aplicacao.IConsultaResultado;
import dev.sauloaraujo.d5.framework.aplicacao.IOrdenacao;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;
import dev.sauloaraujo.d5.framework.aplicacao.impl.ConsultaResultado;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public abstract class AgregadoAplicacaoRepositorioAbstrato<P extends IProjecao<?>, O extends Enum<O> & IOrdenacao<P>, C extends IConsulta<O>>
		implements IAgregadoAplicacaoRepositorio<P, C> {
	@Override
	public IConsultaResultado<P> pesquisar(C consulta) {
		Objects.requireNonNull(consulta);
		var pagina = pesquisar(consulta, paraPageable(consulta));
		return new ConsultaResultado<>(pagina.getContent(), pagina.getTotalElements());
	}

	protected abstract Page<P> pesquisar(C consulta, Pageable pageable);

	protected abstract String paraPropriedadeOrdenacao(O ordenacao);

	protected @Nullable String paraLike(Optional<String> trechoOpcional) {
		Objects.requireNonNull(trechoOpcional);
		return trechoOpcional.map(trecho -> "%" + EscapeCharacter.DEFAULT.escape(trecho.toLowerCase()) + "%")
				.orElse(null);
	}

	private Pageable paraPageable(C consulta) {
		var indicePagina = consulta.getIndicePagina();
		var tamanhoPagina = consulta.getTamanhoPagina();
		var ordenacao = paraOrdenacao(consulta);
		return PageRequest.of(indicePagina, tamanhoPagina, ordenacao);
	}

	private Sort paraOrdenacao(C consulta) {
		return consulta.getOrdenacao().map(ordenacao -> {
			var direcao = consulta.isDescendente() ? Sort.Direction.DESC : Sort.Direction.ASC;
			return Sort.by(direcao, paraPropriedadeOrdenacao(ordenacao));
		}).orElse(Sort.unsorted());
	}
}