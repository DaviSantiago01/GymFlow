package com.example.exemplo.infraestrutura.geografia.unidadefederativa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativa;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaCodigoIbge;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaSigla;
import com.example.exemplo.infraestrutura.geografia.municipio.IMunicipioRepositorioJpa;
import com.example.exemplo.infraestrutura.geografia.municipio.MunicipioJpa;

import dev.sauloaraujo.d5.framework.dominio.AgregadoInexistenteExcecao;

@DataJpaTest
class UnidadeFederativaRepositorioTest {
	@Autowired
	private IUnidadeFederativaRepositorioJpa repositorioJpa;

	@Autowired
	private IMunicipioRepositorioJpa municipioRepositorioJpa;

	private UnidadeFederativaRepositorio repositorio;

	@BeforeEach
	void configurar() {
		repositorio = new UnidadeFederativaRepositorio(repositorioJpa);
	}

	private static UnidadeFederativa criar(String codigoIbge, String sigla, String nome) {
		return new UnidadeFederativa(new UnidadeFederativaCodigoIbge(codigoIbge), new UnidadeFederativaSigla(sigla),
				nome);
	}

	@Test
	void incluiEBuscaPorIdentificador() {
		repositorio.incluir(criar("35", "SP", "São Paulo"));

		var unidadeFederativa = repositorio.buscarPorIdentificador(new UnidadeFederativaCodigoIbge("35"));

		assertEquals(new UnidadeFederativaSigla("SP"), unidadeFederativa.getSigla());
		assertEquals("São Paulo", unidadeFederativa.getNome());
	}

	@Test
	void buscaPorIdentificadorInexistenteLancaExcecao() {
		assertThrows(AgregadoInexistenteExcecao.class,
				() -> repositorio.buscarPorIdentificador(new UnidadeFederativaCodigoIbge("35")));
	}

	@Test
	void existePorIdentificador() {
		repositorio.incluir(criar("35", "SP", "São Paulo"));

		assertTrue(repositorio.existePorIdentificador(new UnidadeFederativaCodigoIbge("35")));
		assertFalse(repositorio.existePorIdentificador(new UnidadeFederativaCodigoIbge("33")));
	}

	@Test
	void altera() {
		repositorio.incluir(criar("35", "SP", "São Paulo"));

		repositorio.alterar(criar("35", "SP", "São Paulo Alterado"));

		var unidadeFederativa = repositorio.buscarPorIdentificador(new UnidadeFederativaCodigoIbge("35"));
		assertEquals("São Paulo Alterado", unidadeFederativa.getNome());
	}

	@Test
	void exclui() {
		repositorio.incluir(criar("35", "SP", "São Paulo"));

		repositorio.excluir(new UnidadeFederativaCodigoIbge("35"));

		assertFalse(repositorio.existePorIdentificador(new UnidadeFederativaCodigoIbge("35")));
	}

	@Test
	void existePorSiglaConsiderandoTodasAsUnidadesFederativas() {
		repositorio.incluir(criar("35", "SP", "São Paulo"));

		assertTrue(repositorio.existePorSigla(new UnidadeFederativaSigla("SP"), Optional.empty()));
		assertFalse(repositorio.existePorSigla(new UnidadeFederativaSigla("RJ"), Optional.empty()));
	}

	@Test
	void existePorSiglaIgnorandoOProprioIdentificador() {
		repositorio.incluir(criar("35", "SP", "São Paulo"));

		var identificador = Optional.of(new UnidadeFederativaCodigoIbge("35"));

		assertFalse(repositorio.existePorSigla(new UnidadeFederativaSigla("SP"), identificador));
	}

	@Test
	void existePorSiglaNaoIgnorandoUmaUnidadeFederativaDiferente() {
		repositorio.incluir(criar("35", "SP", "São Paulo"));
		repositorio.incluir(criar("33", "RJ", "Rio de Janeiro"));

		var identificador = Optional.of(new UnidadeFederativaCodigoIbge("33"));

		assertTrue(repositorio.existePorSigla(new UnidadeFederativaSigla("SP"), identificador));
	}

	@Test
	void existePorNomeConsiderandoTodasAsUnidadesFederativas() {
		repositorio.incluir(criar("35", "SP", "São Paulo"));

		assertTrue(repositorio.existePorNome("São Paulo", Optional.empty()));
		assertFalse(repositorio.existePorNome("Rio de Janeiro", Optional.empty()));
	}

	@Test
	void existePorNomeIgnorandoOProprioIdentificador() {
		repositorio.incluir(criar("35", "SP", "São Paulo"));

		var identificador = Optional.of(new UnidadeFederativaCodigoIbge("35"));

		assertFalse(repositorio.existePorNome("São Paulo", identificador));
	}

	@Test
	void existePorNomeNaoIgnorandoUmaUnidadeFederativaDiferente() {
		repositorio.incluir(criar("35", "SP", "São Paulo"));
		repositorio.incluir(criar("33", "RJ", "Rio de Janeiro"));

		var identificador = Optional.of(new UnidadeFederativaCodigoIbge("33"));

		assertTrue(repositorio.existePorNome("São Paulo", identificador));
	}

	@Test
	void existePorNomeIgnorandoMaiusculasEMinusculas() {
		repositorio.incluir(criar("35", "SP", "São Paulo"));

		assertTrue(repositorio.existePorNome("SÃO PAULO", Optional.empty()));
	}

	@Test
	void existeReferenciaPorMunicipio() {
		repositorio.incluir(criar("35", "SP", "São Paulo"));
		repositorio.incluir(criar("33", "RJ", "Rio de Janeiro"));
		municipioRepositorioJpa
				.save(new MunicipioJpa("3550308", "São Paulo", repositorioJpa.getReferenceById("35"), false));

		assertTrue(repositorio.existeReferencia(new UnidadeFederativaCodigoIbge("35")));
		assertFalse(repositorio.existeReferencia(new UnidadeFederativaCodigoIbge("33")));
	}
}
