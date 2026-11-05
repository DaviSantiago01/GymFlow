package com.example.exemplo.infraestrutura.geografia.municipio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.exemplo.aplicacao.geografia.municipio.MunicipioConsulta;
import com.example.exemplo.aplicacao.geografia.municipio.MunicipioOrdenacao;
import com.example.exemplo.dominio.geografia.municipio.Municipio;
import com.example.exemplo.dominio.geografia.municipio.MunicipioCodigoIbge;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaCodigoIbge;
import com.example.exemplo.infraestrutura.geografia.unidadefederativa.IUnidadeFederativaRepositorioJpa;
import com.example.exemplo.infraestrutura.geografia.unidadefederativa.UnidadeFederativaJpa;

@DataJpaTest
class MunicipioAplicacaoRepositorioTest {
	private static final UnidadeFederativaCodigoIbge RIO_DE_JANEIRO = new UnidadeFederativaCodigoIbge("33");

	@Autowired
	private IMunicipioRepositorioJpa repositorioJpa;

	@Autowired
	private IUnidadeFederativaRepositorioJpa unidadeFederativaRepositorioJpa;

	private MunicipioRepositorio repositorio;

	private MunicipioAplicacaoRepositorio repositorioAplicacao;

	@BeforeEach
	void configurar() {
		repositorio = new MunicipioRepositorio(repositorioJpa, unidadeFederativaRepositorioJpa);
		repositorioAplicacao = new MunicipioAplicacaoRepositorio(repositorioJpa);

		unidadeFederativaRepositorioJpa.save(new UnidadeFederativaJpa("35", "SP", "São Paulo"));
		unidadeFederativaRepositorioJpa.save(new UnidadeFederativaJpa("33", "RJ", "Rio de Janeiro"));
	}

	private static Municipio criar(String codigoIbge, String nome) {
		return new Municipio(new MunicipioCodigoIbge(codigoIbge), nome, Optional.empty());
	}

	private static MunicipioConsulta consultar(Optional<String> nome, Optional<String> unidadeFederativa) {
		return consultar(MunicipioOrdenacao.NOME, nome, unidadeFederativa);
	}

	private static MunicipioConsulta consultar(MunicipioOrdenacao ordenacao, Optional<String> nome,
			Optional<String> unidadeFederativa) {
		return new MunicipioConsulta(Optional.of(ordenacao), Optional.empty(), Optional.empty(), Optional.empty(), nome,
				unidadeFederativa);
	}

	@Test
	void pesquisaRetornaProjecaoOrdenadaPorNome() {
		repositorio.incluir(criar("3550308", "São Paulo"));
		repositorio.incluir(criar("3304557", "Rio de Janeiro").extinguir());

		var resultado = repositorioAplicacao.pesquisar(consultar(Optional.empty(), Optional.empty()));

		assertEquals(2, resultado.getTotal());

		var primeira = resultado.getProjecoes().get(0);
		assertEquals(new MunicipioCodigoIbge("3304557"), primeira.getIdentificador());
		assertEquals("Rio de Janeiro", primeira.getNome());
		assertEquals(RIO_DE_JANEIRO, primeira.getUnidadeFederativa());
		assertEquals("Rio de Janeiro", primeira.getUnidadeFederativaNome());
		assertTrue(primeira.isExtinto());

		assertEquals("São Paulo", resultado.getProjecoes().get(1).getNome());
	}

	@Test
	void pesquisaOrdenadaPorNomeDaUnidadeFederativa() {
		repositorio.incluir(criar("3509502", "Campinas"));
		repositorio.incluir(criar("3303302", "Niterói"));

		var resultado = repositorioAplicacao
				.pesquisar(consultar(MunicipioOrdenacao.UNIDADE_FEDERATIVA_NOME, Optional.empty(), Optional.empty()));

		assertEquals("Niterói", resultado.getProjecoes().get(0).getNome());
		assertEquals("Campinas", resultado.getProjecoes().get(1).getNome());
	}

	@Test
	void pesquisaFiltrandoPorTrechoDoNome() {
		repositorio.incluir(criar("3550308", "São Paulo"));
		repositorio.incluir(criar("3304557", "Rio de Janeiro"));

		var resultado = repositorioAplicacao.pesquisar(consultar(Optional.of("paulo"), Optional.empty()));

		assertEquals(1, resultado.getTotal());
		assertEquals("São Paulo", resultado.getProjecoes().get(0).getNome());
	}

	@Test
	void pesquisaFiltrandoPorUnidadeFederativa() {
		repositorio.incluir(criar("3550308", "São Paulo"));
		repositorio.incluir(criar("3304557", "Rio de Janeiro"));

		var resultado = repositorioAplicacao.pesquisar(consultar(Optional.empty(), Optional.of("33")));

		assertEquals(1, resultado.getTotal());
		assertEquals("Rio de Janeiro", resultado.getProjecoes().get(0).getNome());
	}
}
