package com.example.exemplo.infraestrutura.crm.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.exemplo.aplicacao.crm.cliente.ClienteConsulta;
import com.example.exemplo.aplicacao.crm.cliente.ClienteOrdenacao;
import com.example.exemplo.dominio.crm.cliente.Cliente;
import com.example.exemplo.dominio.crm.cliente.Cpf;
import com.example.exemplo.dominio.crm.cliente.Endereco;
import com.example.exemplo.dominio.crm.cliente.EnderecoCep;
import com.example.exemplo.dominio.geografia.municipio.MunicipioCodigoIbge;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaCodigoIbge;
import com.example.exemplo.infraestrutura.geografia.municipio.IMunicipioRepositorioJpa;
import com.example.exemplo.infraestrutura.geografia.municipio.MunicipioJpa;
import com.example.exemplo.infraestrutura.geografia.unidadefederativa.IUnidadeFederativaRepositorioJpa;
import com.example.exemplo.infraestrutura.geografia.unidadefederativa.UnidadeFederativaJpa;

@DataJpaTest
class ClienteAplicacaoRepositorioTest {
	@Autowired
	private IClienteRepositorioJpa repositorioJpa;

	@Autowired
	private IMunicipioRepositorioJpa municipioRepositorioJpa;

	@Autowired
	private IUnidadeFederativaRepositorioJpa unidadeFederativaRepositorioJpa;

	private ClienteRepositorio repositorio;

	private ClienteAplicacaoRepositorio repositorioAplicacao;

	@BeforeEach
	void configurar() {
		repositorio = new ClienteRepositorio(repositorioJpa, municipioRepositorioJpa);
		repositorioAplicacao = new ClienteAplicacaoRepositorio(repositorioJpa);

		unidadeFederativaRepositorioJpa.save(new UnidadeFederativaJpa("35", "SP", "São Paulo"));
		unidadeFederativaRepositorioJpa.save(new UnidadeFederativaJpa("33", "RJ", "Rio de Janeiro"));
		municipioRepositorioJpa.save(new MunicipioJpa("3550308", "São Paulo",
				unidadeFederativaRepositorioJpa.getReferenceById("35"), false));
		municipioRepositorioJpa.save(
				new MunicipioJpa("3548500", "Santos", unidadeFederativaRepositorioJpa.getReferenceById("35"), false));
		municipioRepositorioJpa.save(new MunicipioJpa("3304557", "Rio de Janeiro",
				unidadeFederativaRepositorioJpa.getReferenceById("33"), false));
	}

	private static Cliente criar(String cpf, String nome, String municipio) {
		var endereco = new Endereco("Rua A", "10", Optional.empty(), "Centro", new EnderecoCep("01310100"),
				new MunicipioCodigoIbge(municipio));
		return new Cliente(Optional.empty(), new Cpf(cpf), nome, endereco);
	}

	private static ClienteConsulta consultar(Optional<String> nome, Optional<String> municipio,
			Optional<String> unidadeFederativa) {
		return consultar(ClienteOrdenacao.NOME, nome, municipio, unidadeFederativa);
	}

	private static ClienteConsulta consultar(ClienteOrdenacao ordenacao, Optional<String> nome,
			Optional<String> municipio, Optional<String> unidadeFederativa) {
		return new ClienteConsulta(Optional.of(ordenacao), Optional.empty(), Optional.empty(), Optional.empty(), nome,
				municipio, unidadeFederativa);
	}

	@Test
	void pesquisaRetornaProjecaoOrdenadaPorNome() {
		repositorio.incluir(criar("11111111111", "Maria", "3550308"));
		repositorio.incluir(criar("22222222222", "Ana", "3304557"));

		var resultado = repositorioAplicacao.pesquisar(consultar(Optional.empty(), Optional.empty(), Optional.empty()));

		assertEquals(2, resultado.getTotal());

		var primeira = resultado.getProjecoes().get(0);
		assertEquals("Ana", primeira.getNome());
		assertEquals(new MunicipioCodigoIbge("3304557"), primeira.getMunicipio());
		assertEquals("Rio de Janeiro", primeira.getMunicipioNome());
		assertEquals(new UnidadeFederativaCodigoIbge("33"), primeira.getUnidadeFederativa());
		assertEquals("Rio de Janeiro", primeira.getUnidadeFederativaNome());

		assertEquals("Maria", resultado.getProjecoes().get(1).getNome());
	}

	@Test
	void pesquisaOrdenadaPorNomeDoMunicipio() {
		repositorio.incluir(criar("11111111111", "Ana", "3550308"));
		repositorio.incluir(criar("22222222222", "Maria", "3548500"));

		var resultado = repositorioAplicacao.pesquisar(
				consultar(ClienteOrdenacao.MUNICIPIO_NOME, Optional.empty(), Optional.empty(), Optional.empty()));

		assertEquals("Maria", resultado.getProjecoes().get(0).getNome());
		assertEquals("Ana", resultado.getProjecoes().get(1).getNome());
	}

	@Test
	void pesquisaOrdenadaPorNomeDaUnidadeFederativa() {
		repositorio.incluir(criar("11111111111", "Ana", "3550308"));
		repositorio.incluir(criar("22222222222", "Maria", "3304557"));

		var resultado = repositorioAplicacao.pesquisar(consultar(ClienteOrdenacao.UNIDADE_FEDERATIVA_NOME,
				Optional.empty(), Optional.empty(), Optional.empty()));

		assertEquals("Maria", resultado.getProjecoes().get(0).getNome());
		assertEquals("Ana", resultado.getProjecoes().get(1).getNome());
	}

	@Test
	void pesquisaFiltrandoPorTrechoDoNome() {
		repositorio.incluir(criar("11111111111", "Maria", "3550308"));
		repositorio.incluir(criar("22222222222", "Ana", "3304557"));

		var resultado = repositorioAplicacao
				.pesquisar(consultar(Optional.of("mar"), Optional.empty(), Optional.empty()));

		assertEquals(1, resultado.getTotal());
		assertEquals("Maria", resultado.getProjecoes().get(0).getNome());
	}

	@Test
	void pesquisaFiltrandoPorMunicipio() {
		repositorio.incluir(criar("11111111111", "Maria", "3550308"));
		repositorio.incluir(criar("22222222222", "Ana", "3304557"));

		var resultado = repositorioAplicacao
				.pesquisar(consultar(Optional.empty(), Optional.of("3304557"), Optional.empty()));

		assertEquals(1, resultado.getTotal());
		assertEquals("Ana", resultado.getProjecoes().get(0).getNome());
	}

	@Test
	void pesquisaFiltrandoPorUnidadeFederativa() {
		repositorio.incluir(criar("11111111111", "Maria", "3550308"));
		repositorio.incluir(criar("22222222222", "Ana", "3548500"));
		repositorio.incluir(criar("33333333333", "Beatriz", "3304557"));

		var resultado = repositorioAplicacao
				.pesquisar(consultar(Optional.empty(), Optional.empty(), Optional.of("35")));

		assertEquals(2, resultado.getTotal());
		assertEquals("Ana", resultado.getProjecoes().get(0).getNome());
		assertEquals("Maria", resultado.getProjecoes().get(1).getNome());
	}
}
