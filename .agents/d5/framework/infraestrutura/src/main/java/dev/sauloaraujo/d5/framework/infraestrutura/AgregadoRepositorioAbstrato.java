package dev.sauloaraujo.d5.framework.infraestrutura;

import java.util.Objects;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.sauloaraujo.d5.framework.dominio.AgregadoInexistenteExcecao;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;
import dev.sauloaraujo.d5.framework.dominio.IIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public abstract class AgregadoRepositorioAbstrato<I1 extends IIdentificador, A1 extends IAgregado<I1>, I2, A2 extends IAgregadoJpa<A1>>
		implements IAgregadoRepositorio<I1, A1> {
	private final JpaRepository<A2, I2> repositorioJpa;
	private final String inexistenteModelo;

	protected AgregadoRepositorioAbstrato(JpaRepository<A2, I2> repositorioJpa, String inexistenteModelo) {
		this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
		this.inexistenteModelo = Objects.requireNonNull(inexistenteModelo);
	}

	@Override
	public A1 incluir(A1 agregado) {
		Objects.requireNonNull(agregado);
		var entidadeJpa = repositorioJpa.save(paraJpa(agregado));
		return paraAgregado(entidadeJpa);
	}

	protected abstract A2 paraJpa(A1 agregado);

	protected abstract A1 paraAgregado(A2 entidadeJpa);

	@Override
	public A1 buscarPorIdentificador(I1 identificador) {
		Objects.requireNonNull(identificador);
		var identificadorJpa = paraIdentificadorJpa(identificador);
		var entidadeJpa = repositorioJpa.findById(identificadorJpa).orElseThrow(() -> {
			var mensagem = inexistenteModelo.formatted(identificador);
			return new AgregadoInexistenteExcecao(mensagem);
		});
		return paraAgregado(entidadeJpa);
	}

	protected abstract I2 paraIdentificadorJpa(I1 identificador);

	@Override
	public void alterar(A1 agregado) {
		Objects.requireNonNull(agregado);
		repositorioJpa.save(paraJpa(agregado));
	}

	@Override
	public void excluir(I1 identificador) {
		Objects.requireNonNull(identificador);
		repositorioJpa.deleteById(paraIdentificadorJpa(identificador));
	}

	@Override
	public boolean existePorIdentificador(I1 identificador) {
		Objects.requireNonNull(identificador);
		return repositorioJpa.existsById(paraIdentificadorJpa(identificador));
	}
}
