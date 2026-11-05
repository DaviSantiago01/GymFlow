package com.example.exemplo.infraestrutura.geografia.unidadefederativa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.exemplo.aplicacao.geografia.unidadefederativa.UnidadeFederativaConsulta;
import com.example.exemplo.aplicacao.geografia.unidadefederativa.UnidadeFederativaOrdenacao;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativa;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaCodigoIbge;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaSigla;

import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

@DataJpaTest
class UnidadeFederativaAplicacaoRepositorioTest {
	@Autowired
	private IUnidadeFederativaRepositorioJpa repositorioJpa;

	private AgregadoRepositorioAbstrato<UnidadeFederativaCodigoIbge, UnidadeFederativa, String, UnidadeFederativaJpa> repositorio;

	private UnidadeFederativaAplicacaoRepositorio repositorioAplicacao;

	@BeforeEach
	void configurar() {
		repositorio = new UnidadeFederativaRepositorio(repositorioJpa);
		repositorioAplicacao = new UnidadeFederativaAplicacaoRepositorio(repositorioJpa);
	}

	private static UnidadeFederativa criar(String codigoIbge, String sigla, String nome) {
		return new UnidadeFederativa(new UnidadeFederativaCodigoIbge(codigoIbge), new UnidadeFederativaSigla(sigla),
				nome);
	}

	private static UnidadeFederativaConsulta consultar() {
		return consultar(UnidadeFederativaOrdenacao.NOME, Optional.empty(), Optional.empty());
	}

	private static UnidadeFederativaConsulta consultar(UnidadeFederativaOrdenacao ordenacao, Optional<String> sigla,
			Optional<String> nome) {
		return new UnidadeFederativaConsulta(Optional.of(ordenacao), Optional.empty(), Optional.empty(),
				Optional.empty(), sigla, nome);
	}

	@Test
	void pesquisaRetornaProjecaoComIdentificadorSiglaENome() {
		repositorio.incluir(criar("35", "SP", "São Paulo"));
		repositorio.incluir(criar("33", "RJ", "Rio de Janeiro"));

		var resultado = repositorioAplicacao.pesquisar(consultar());

		assertEquals(2, resultado.getTotal());
		assertEquals(2, resultado.getProjecoes().size());

		var primeira = resultado.getProjecoes().get(0);
		assertEquals(new UnidadeFederativaCodigoIbge("33"), primeira.getIdentificador());
		assertEquals(new UnidadeFederativaSigla("RJ"), primeira.getSigla());
		assertEquals("Rio de Janeiro", primeira.getNome());

		var segunda = resultado.getProjecoes().get(1);
		assertEquals(new UnidadeFederativaCodigoIbge("35"), segunda.getIdentificador());
		assertEquals(new UnidadeFederativaSigla("SP"), segunda.getSigla());
		assertEquals("São Paulo", segunda.getNome());
	}

	@Test
	void pesquisaSemUnidadesFederativasRetornaResultadoVazio() {
		var resultado = repositorioAplicacao.pesquisar(consultar());

		assertEquals(0, resultado.getTotal());
		assertEquals(0, resultado.getProjecoes().size());
	}

	@Test
	void pesquisaFiltrandoPorTrechoDoNome() {
		repositorio.incluir(criar("35", "SP", "São Paulo"));
		repositorio.incluir(criar("33", "RJ", "Rio de Janeiro"));

		var resultado = repositorioAplicacao
				.pesquisar(consultar(UnidadeFederativaOrdenacao.NOME, Optional.empty(), Optional.of("paulo")));

		assertEquals(1, resultado.getTotal());
		assertEquals("São Paulo", resultado.getProjecoes().get(0).getNome());
	}

	@Test
	void pesquisaFiltrandoPorTrechoDaSigla() {
		repositorio.incluir(criar("35", "SP", "São Paulo"));
		repositorio.incluir(criar("33", "RJ", "Rio de Janeiro"));

		var resultado = repositorioAplicacao
				.pesquisar(consultar(UnidadeFederativaOrdenacao.NOME, Optional.of("r"), Optional.empty()));

		assertEquals(1, resultado.getTotal());
		assertEquals("Rio de Janeiro", resultado.getProjecoes().get(0).getNome());
	}

	@Test
	void pesquisaOrdenadaPorSigla() {
		repositorio.incluir(criar("33", "RJ", "Rio de Janeiro"));
		repositorio.incluir(criar("12", "AC", "Acre"));
		repositorio.incluir(criar("27", "AL", "Alagoas"));

		var resultado = repositorioAplicacao
				.pesquisar(consultar(UnidadeFederativaOrdenacao.SIGLA, Optional.empty(), Optional.empty()));

		assertEquals(new UnidadeFederativaSigla("AC"), resultado.getProjecoes().get(0).getSigla());
		assertEquals(new UnidadeFederativaSigla("AL"), resultado.getProjecoes().get(1).getSigla());
		assertEquals(new UnidadeFederativaSigla("RJ"), resultado.getProjecoes().get(2).getSigla());
	}
}
