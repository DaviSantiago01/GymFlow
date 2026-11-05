package com.example.exemplo.dominio.geografia.unidadefederativa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.example.exemplo.dominio.Autorizacao;
import com.example.exemplo.dominio.geografia.unidadefederativa.spi.IUnidadeFederativaRepositorio;

import dev.sauloaraujo.d5.framework.dominio.AgregadoExistenteExcecao;
import dev.sauloaraujo.d5.framework.dominio.AgregadoInexistenteExcecao;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

public final class UnidadeFederativaStepDefinitions {
	private final Map<String, UnidadeFederativa> unidadesFederativas = new HashMap<>();
	private final Set<String> referenciadas = new HashSet<>();
	private final ISegurancaServico segurancaServico = mock(ISegurancaServico.class);
	private final IUnidadeFederativaRepositorio repositorio = mock(IUnidadeFederativaRepositorio.class);
	private final UnidadeFederativaServico servico = new UnidadeFederativaServico(segurancaServico, repositorio);

	private UnidadeFederativa resultadoInclusao;
	private UnidadeFederativa resultadoBusca;
	private RuntimeException excecaoCapturada;

	public UnidadeFederativaStepDefinitions() {
		when(repositorio.existePorIdentificador(any()))
				.thenAnswer(chamada -> unidadesFederativas.containsKey(chave(chamada.getArgument(0))));

		when(repositorio.buscarPorIdentificador(any())).thenAnswer(chamada -> {
			UnidadeFederativaCodigoIbge codigoIbge = chamada.getArgument(0);
			var unidadeFederativa = unidadesFederativas.get(chave(codigoIbge));
			if (unidadeFederativa == null) {
				throw new AgregadoInexistenteExcecao(
						String.format(UnidadeFederativaServico.IDENTIFICADOR_INEXISTENTE_MODELO, codigoIbge));
			}
			return unidadeFederativa;
		});

		when(repositorio.existePorSigla(any(), any())).thenAnswer(chamada -> {
			UnidadeFederativaSigla sigla = chamada.getArgument(0);
			Optional<UnidadeFederativaCodigoIbge> identificadorIgnorado = chamada.getArgument(1);
			return unidadesFederativas.values().stream().anyMatch(
					candidata -> candidata.getSigla().equals(sigla) && !foiIgnorada(candidata, identificadorIgnorado));
		});

		when(repositorio.existePorNome(any(), any())).thenAnswer(chamada -> {
			String nome = chamada.getArgument(0);
			Optional<UnidadeFederativaCodigoIbge> identificadorIgnorado = chamada.getArgument(1);
			return unidadesFederativas.values().stream()
					.anyMatch(candidata -> candidata.getNome().equalsIgnoreCase(nome)
							&& !foiIgnorada(candidata, identificadorIgnorado));
		});

		when(repositorio.existeReferencia(any()))
				.thenAnswer(chamada -> referenciadas.contains(chave(chamada.getArgument(0))));

		when(repositorio.incluir(any())).thenAnswer(chamada -> {
			UnidadeFederativa unidadeFederativa = chamada.getArgument(0);
			unidadesFederativas.put(chave(unidadeFederativa.getIdentificador().orElseThrow()), unidadeFederativa);
			return unidadeFederativa;
		});

		doAnswer(chamada -> {
			UnidadeFederativa unidadeFederativa = chamada.getArgument(0);
			unidadesFederativas.put(chave(unidadeFederativa.getIdentificador().orElseThrow()), unidadeFederativa);
			return null;
		}).when(repositorio).alterar(any());

		doAnswer(chamada -> {
			UnidadeFederativaCodigoIbge codigoIbge = chamada.getArgument(0);
			unidadesFederativas.remove(chave(codigoIbge));
			return null;
		}).when(repositorio).excluir(any());
	}

	private static String chave(UnidadeFederativaCodigoIbge codigoIbge) {
		return codigoIbge.toString();
	}

	private static boolean foiIgnorada(UnidadeFederativa candidata,
			Optional<UnidadeFederativaCodigoIbge> identificadorIgnorado) {
		return identificadorIgnorado.isPresent() && candidata.getIdentificador().equals(identificadorIgnorado);
	}

	private static UnidadeFederativa criar(String codigoIbge, String sigla, String nome) {
		return new UnidadeFederativa(new UnidadeFederativaCodigoIbge(codigoIbge), new UnidadeFederativaSigla(sigla),
				nome);
	}

	@Dado("que não existe nenhuma unidade federativa cadastrada")
	public void que_nao_existe_nenhuma_unidade_federativa_cadastrada() {
		unidadesFederativas.clear();
		referenciadas.clear();
	}

	@Dado("que sou um usuário administrador")
	public void que_sou_um_usuario_administrador() {
		when(segurancaServico.possuiAutorizacao(Autorizacao.ADMINISTRADOR.name())).thenReturn(true);
	}

	@Dado("que sou um usuário sem autorização de administrador")
	public void que_sou_um_usuario_sem_autorizacao_de_administrador() {
		when(segurancaServico.possuiAutorizacao(Autorizacao.ADMINISTRADOR.name())).thenReturn(false);
	}

	@Dado("que existe uma unidade federativa com código IBGE {string}, sigla {string} e nome {string}")
	public void que_existe_uma_unidade_federativa(String codigoIbge, String sigla, String nome) {
		var unidadeFederativa = criar(codigoIbge, sigla, nome);
		unidadesFederativas.put(chave(unidadeFederativa.getIdentificador().orElseThrow()), unidadeFederativa);
	}

	@Dado("que a unidade federativa de código IBGE {string} é referenciada")
	public void que_a_unidade_federativa_e_referenciada(String codigoIbge) {
		referenciadas.add(codigoIbge);
	}

	@Quando("eu incluo a unidade federativa de código IBGE {string}, sigla {string} e nome {string}")
	public void eu_incluo_a_unidade_federativa(String codigoIbge, String sigla, String nome) {
		incluir(codigoIbge, sigla, nome);
	}

	@Quando("eu tento incluir a unidade federativa de código IBGE {string}, sigla {string} e nome {string}")
	public void eu_tento_incluir_a_unidade_federativa(String codigoIbge, String sigla, String nome) {
		incluir(codigoIbge, sigla, nome);
	}

	private void incluir(String codigoIbge, String sigla, String nome) {
		try {
			resultadoInclusao = servico.incluir(criar(codigoIbge, sigla, nome));
		} catch (RuntimeException excecao) {
			excecaoCapturada = excecao;
		}
	}

	@Quando("eu busco a unidade federativa de código IBGE {string}")
	public void eu_busco_a_unidade_federativa(String codigoIbge) {
		buscar(codigoIbge);
	}

	@Quando("eu tento buscar a unidade federativa de código IBGE {string}")
	public void eu_tento_buscar_a_unidade_federativa(String codigoIbge) {
		buscar(codigoIbge);
	}

	private void buscar(String codigoIbge) {
		try {
			resultadoBusca = servico.buscar(new UnidadeFederativaCodigoIbge(codigoIbge));
		} catch (RuntimeException excecao) {
			excecaoCapturada = excecao;
		}
	}

	@Quando("eu altero a unidade federativa de código IBGE {string} para sigla {string} e nome {string}")
	public void eu_altero_a_unidade_federativa(String codigoIbge, String sigla, String nome) {
		alterar(codigoIbge, sigla, nome);
	}

	@Quando("eu tento alterar a unidade federativa de código IBGE {string} para sigla {string} e nome {string}")
	public void eu_tento_alterar_a_unidade_federativa(String codigoIbge, String sigla, String nome) {
		alterar(codigoIbge, sigla, nome);
	}

	private void alterar(String codigoIbge, String sigla, String nome) {
		try {
			servico.alterar(criar(codigoIbge, sigla, nome));
		} catch (RuntimeException excecao) {
			excecaoCapturada = excecao;
		}
	}

	@Quando("eu excluo a unidade federativa de código IBGE {string}")
	public void eu_excluo_a_unidade_federativa(String codigoIbge) {
		excluir(codigoIbge);
	}

	@Quando("eu tento excluir a unidade federativa de código IBGE {string}")
	public void eu_tento_excluir_a_unidade_federativa(String codigoIbge) {
		excluir(codigoIbge);
	}

	private void excluir(String codigoIbge) {
		try {
			servico.excluir(new UnidadeFederativaCodigoIbge(codigoIbge));
		} catch (RuntimeException excecao) {
			excecaoCapturada = excecao;
		}
	}

	@Entao("a unidade federativa é incluída com sucesso")
	public void a_unidade_federativa_e_incluida_com_sucesso() {
		assertNull(excecaoCapturada);
		assertNotNull(resultadoInclusao);
	}

	@Entao("a unidade federativa de código IBGE {string} passa a existir")
	public void a_unidade_federativa_passa_a_existir(String codigoIbge) {
		assertTrue(unidadesFederativas.containsKey(codigoIbge));
	}

	@Entao("a unidade federativa de código IBGE {string} continua a existir")
	public void a_unidade_federativa_continua_a_existir(String codigoIbge) {
		assertTrue(unidadesFederativas.containsKey(codigoIbge));
	}

	@Entao("a inclusão é rejeitada por falta de autorização")
	public void a_inclusao_e_rejeitada_por_falta_de_autorizacao() {
		assertInstanceOf(NaoAutorizadoExcecao.class, excecaoCapturada);
	}

	@Entao("a inclusão é rejeitada porque já existe uma unidade federativa com o identificador {string}")
	public void a_inclusao_e_rejeitada_pelo_identificador(String codigoIbge) {
		assertMensagemDeExistencia(UnidadeFederativaServico.IDENTIFICADOR_EXISTENTE_MODELO, codigoIbge);
	}

	@Entao("a inclusão é rejeitada porque já existe uma unidade federativa com a sigla {string}")
	public void a_inclusao_e_rejeitada_pela_sigla(String sigla) {
		assertMensagemDeExistencia(UnidadeFederativaServico.SIGLA_EXISTENTE_MODELO, sigla);
	}

	@Entao("a inclusão é rejeitada porque já existe uma unidade federativa com o nome {string}")
	public void a_inclusao_e_rejeitada_pelo_nome(String nome) {
		assertMensagemDeExistencia(UnidadeFederativaServico.NOME_EXISTENTE_MODELO, nome);
	}

	@Entao("a unidade federativa encontrada tem sigla {string} e nome {string}")
	public void a_unidade_federativa_encontrada_tem(String sigla, String nome) {
		assertEquals(new UnidadeFederativaSigla(sigla), resultadoBusca.getSigla());
		assertEquals(nome, resultadoBusca.getNome());
	}

	@Entao("a busca é rejeitada porque não existe unidade federativa com o identificador {string}")
	public void a_busca_e_rejeitada_por_inexistencia(String codigoIbge) {
		assertMensagemDeInexistencia(codigoIbge);
	}

	@Entao("a unidade federativa é alterada com sucesso")
	public void a_unidade_federativa_e_alterada_com_sucesso() {
		assertNull(excecaoCapturada);
	}

	@Entao("a unidade federativa de código IBGE {string} passa a ter sigla {string} e nome {string}")
	public void a_unidade_federativa_passa_a_ter(String codigoIbge, String sigla, String nome) {
		var unidadeFederativa = unidadesFederativas.get(codigoIbge);
		assertNotNull(unidadeFederativa);
		assertEquals(new UnidadeFederativaSigla(sigla), unidadeFederativa.getSigla());
		assertEquals(nome, unidadeFederativa.getNome());
	}

	@Entao("a alteração é rejeitada por falta de autorização")
	public void a_alteracao_e_rejeitada_por_falta_de_autorizacao() {
		assertInstanceOf(NaoAutorizadoExcecao.class, excecaoCapturada);
	}

	@Entao("a alteração é rejeitada porque não existe unidade federativa com o identificador {string}")
	public void a_alteracao_e_rejeitada_por_inexistencia(String codigoIbge) {
		assertMensagemDeInexistencia(codigoIbge);
	}

	@Entao("a alteração é rejeitada porque já existe uma unidade federativa com a sigla {string}")
	public void a_alteracao_e_rejeitada_pela_sigla(String sigla) {
		assertMensagemDeExistencia(UnidadeFederativaServico.SIGLA_EXISTENTE_MODELO, sigla);
	}

	@Entao("a unidade federativa é excluída com sucesso")
	public void a_unidade_federativa_e_excluida_com_sucesso() {
		assertNull(excecaoCapturada);
	}

	@Entao("a unidade federativa de código IBGE {string} deixa de existir")
	public void a_unidade_federativa_deixa_de_existir(String codigoIbge) {
		assertFalse(unidadesFederativas.containsKey(codigoIbge));
	}

	@Entao("a exclusão é rejeitada por falta de autorização")
	public void a_exclusao_e_rejeitada_por_falta_de_autorizacao() {
		assertInstanceOf(NaoAutorizadoExcecao.class, excecaoCapturada);
	}

	@Entao("a exclusão é rejeitada porque não existe unidade federativa com o identificador {string}")
	public void a_exclusao_e_rejeitada_por_inexistencia(String codigoIbge) {
		assertMensagemDeInexistencia(codigoIbge);
	}

	@Entao("a exclusão é rejeitada porque a unidade federativa {string} é referenciada")
	public void a_exclusao_e_rejeitada_por_referencia(String codigoIbge) {
		assertInstanceOf(IllegalStateException.class, excecaoCapturada);
		assertEquals(String.format(UnidadeFederativaServico.REFERENCIADA_MODELO, codigoIbge),
				excecaoCapturada.getMessage());
	}

	private void assertMensagemDeExistencia(String modelo, Object argumento) {
		assertInstanceOf(AgregadoExistenteExcecao.class, excecaoCapturada);
		assertEquals(String.format(modelo, argumento), excecaoCapturada.getMessage());
	}

	private void assertMensagemDeInexistencia(Object argumento) {
		assertInstanceOf(AgregadoInexistenteExcecao.class, excecaoCapturada);
		assertEquals(String.format(UnidadeFederativaServico.IDENTIFICADOR_INEXISTENTE_MODELO, argumento),
				excecaoCapturada.getMessage());
	}
}
