package com.example.exemplo.dominio.geografia.municipio;

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
import com.example.exemplo.dominio.geografia.municipio.spi.IMunicipioRepositorio;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaCodigoIbge;
import com.example.exemplo.dominio.geografia.unidadefederativa.UnidadeFederativaServico;
import com.example.exemplo.dominio.geografia.unidadefederativa.spi.IUnidadeFederativaRepositorio;

import dev.sauloaraujo.d5.framework.dominio.AgregadoExistenteExcecao;
import dev.sauloaraujo.d5.framework.dominio.AgregadoInexistenteExcecao;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

public final class MunicipioStepDefinitions {
	private final Map<String, Municipio> municipios = new HashMap<>();
	private final Set<String> unidadesFederativas = new HashSet<>();
	private final Set<String> referenciados = new HashSet<>();
	private final ISegurancaServico segurancaServico = mock(ISegurancaServico.class);
	private final IMunicipioRepositorio repositorio = mock(IMunicipioRepositorio.class);
	private final IUnidadeFederativaRepositorio unidadeFederativaRepositorio = mock(
			IUnidadeFederativaRepositorio.class);
	private final MunicipioServico servico = new MunicipioServico(segurancaServico, repositorio,
			unidadeFederativaRepositorio);

	private Municipio resultadoInclusao;
	private Municipio resultadoBusca;
	private RuntimeException excecaoCapturada;

	public MunicipioStepDefinitions() {
		when(unidadeFederativaRepositorio.existePorIdentificador(any()))
				.thenAnswer(chamada -> unidadesFederativas.contains(chamada.getArgument(0).toString()));

		when(repositorio.existePorIdentificador(any()))
				.thenAnswer(chamada -> municipios.containsKey(chave(chamada.getArgument(0))));

		when(repositorio.buscarPorIdentificador(any())).thenAnswer(chamada -> {
			MunicipioCodigoIbge codigoIbge = chamada.getArgument(0);
			var municipio = municipios.get(chave(codigoIbge));
			if (municipio == null) {
				throw new AgregadoInexistenteExcecao(
						String.format(MunicipioServico.IDENTIFICADOR_INEXISTENTE_MODELO, codigoIbge));
			}
			return municipio;
		});

		when(repositorio.existePorNome(any(), any(), any())).thenAnswer(chamada -> {
			String nome = chamada.getArgument(0);
			UnidadeFederativaCodigoIbge unidadeFederativa = chamada.getArgument(1);
			Optional<MunicipioCodigoIbge> identificadorIgnorado = chamada.getArgument(2);
			return municipios.values().stream()
					.anyMatch(candidato -> candidato.getNome().equalsIgnoreCase(nome)
							&& candidato.getUnidadeFederativa().equals(unidadeFederativa)
							&& !foiIgnorado(candidato, identificadorIgnorado));
		});

		when(repositorio.existeReferencia(any()))
				.thenAnswer(chamada -> referenciados.contains(chave(chamada.getArgument(0))));

		when(repositorio.incluir(any())).thenAnswer(chamada -> {
			Municipio municipio = chamada.getArgument(0);
			municipios.put(chave(municipio.getIdentificador().orElseThrow()), municipio);
			return municipio;
		});

		doAnswer(chamada -> {
			Municipio municipio = chamada.getArgument(0);
			municipios.put(chave(municipio.getIdentificador().orElseThrow()), municipio);
			return null;
		}).when(repositorio).alterar(any());

		doAnswer(chamada -> {
			MunicipioCodigoIbge codigoIbge = chamada.getArgument(0);
			municipios.remove(chave(codigoIbge));
			return null;
		}).when(repositorio).excluir(any());
	}

	private static String chave(MunicipioCodigoIbge codigoIbge) {
		return codigoIbge.toString();
	}

	private static boolean foiIgnorado(Municipio candidato, Optional<MunicipioCodigoIbge> identificadorIgnorado) {
		return identificadorIgnorado.isPresent() && candidato.getIdentificador().equals(identificadorIgnorado);
	}

	private static Municipio criar(String codigoIbge, String nome) {
		return new Municipio(new MunicipioCodigoIbge(codigoIbge), nome, Optional.empty());
	}

	@Dado("que não existe nenhum município cadastrado")
	public void que_nao_existe_nenhum_municipio_cadastrado() {
		municipios.clear();
		unidadesFederativas.clear();
		referenciados.clear();
	}

	@Dado("que existe a unidade federativa de código IBGE {string}")
	public void que_existe_a_unidade_federativa(String codigoIbge) {
		unidadesFederativas.add(codigoIbge);
	}

	@Dado("que sou um administrador do cadastro de municípios")
	public void que_sou_um_administrador_do_cadastro_de_municipios() {
		when(segurancaServico.possuiAutorizacao(Autorizacao.ADMINISTRADOR.name())).thenReturn(true);
	}

	@Dado("que sou um usuário sem autorização para administrar municípios")
	public void que_sou_um_usuario_sem_autorizacao_para_administrar_municipios() {
		when(segurancaServico.possuiAutorizacao(Autorizacao.ADMINISTRADOR.name())).thenReturn(false);
	}

	@Dado("que existe um município com código IBGE {string}, nome {string}")
	public void que_existe_um_municipio(String codigoIbge, String nome) {
		var municipio = criar(codigoIbge, nome);
		municipios.put(chave(municipio.getIdentificador().orElseThrow()), municipio);
	}

	@Dado("que existe um município extinto com código IBGE {string}, nome {string}")
	public void que_existe_um_municipio_extinto(String codigoIbge, String nome) {
		var municipio = criar(codigoIbge, nome).extinguir();
		municipios.put(chave(municipio.getIdentificador().orElseThrow()), municipio);
	}

	@Dado("que o município de código IBGE {string} é referenciado")
	public void que_o_municipio_e_referenciado(String codigoIbge) {
		referenciados.add(codigoIbge);
	}

	@Quando("eu incluo o município de código IBGE {string}, nome {string}")
	public void eu_incluo_o_municipio(String codigoIbge, String nome) {
		incluir(codigoIbge, nome);
	}

	@Quando("eu tento incluir o município de código IBGE {string}, nome {string}")
	public void eu_tento_incluir_o_municipio(String codigoIbge, String nome) {
		incluir(codigoIbge, nome);
	}

	@Quando("eu tento incluir o município extinto de código IBGE {string} e nome {string}")
	public void eu_tento_incluir_o_municipio_extinto(String codigoIbge, String nome) {
		incluir(criar(codigoIbge, nome).extinguir());
	}

	private void incluir(String codigoIbge, String nome) {
		incluir(criar(codigoIbge, nome));
	}

	private void incluir(Municipio municipio) {
		try {
			resultadoInclusao = servico.incluir(municipio);
		} catch (RuntimeException excecao) {
			excecaoCapturada = excecao;
		}
	}

	@Quando("eu busco o município de código IBGE {string}")
	public void eu_busco_o_municipio(String codigoIbge) {
		buscar(codigoIbge);
	}

	@Quando("eu tento buscar o município de código IBGE {string}")
	public void eu_tento_buscar_o_municipio(String codigoIbge) {
		buscar(codigoIbge);
	}

	private void buscar(String codigoIbge) {
		try {
			resultadoBusca = servico.buscar(new MunicipioCodigoIbge(codigoIbge));
		} catch (RuntimeException excecao) {
			excecaoCapturada = excecao;
		}
	}

	@Quando("eu altero o município de código IBGE {string} para nome {string}")
	public void eu_altero_o_municipio(String codigoIbge, String nome) {
		alterar(codigoIbge, nome);
	}

	@Quando("eu tento alterar o município de código IBGE {string} para nome {string}")
	public void eu_tento_alterar_o_municipio(String codigoIbge, String nome) {
		alterar(codigoIbge, nome);
	}

	@Quando("eu tento alterar o município de código IBGE {string} para extinto com nome {string}")
	public void eu_tento_alterar_o_municipio_para_extinto(String codigoIbge, String nome) {
		alterar(criar(codigoIbge, nome).extinguir());
	}

	private void alterar(String codigoIbge, String nome) {
		alterar(criar(codigoIbge, nome));
	}

	private void alterar(Municipio municipio) {
		try {
			servico.alterar(municipio);
		} catch (RuntimeException excecao) {
			excecaoCapturada = excecao;
		}
	}

	@Quando("eu excluo o município de código IBGE {string}")
	public void eu_excluo_o_municipio(String codigoIbge) {
		excluir(codigoIbge);
	}

	@Quando("eu tento excluir o município de código IBGE {string}")
	public void eu_tento_excluir_o_municipio(String codigoIbge) {
		excluir(codigoIbge);
	}

	private void excluir(String codigoIbge) {
		try {
			servico.excluir(new MunicipioCodigoIbge(codigoIbge));
		} catch (RuntimeException excecao) {
			excecaoCapturada = excecao;
		}
	}

	@Quando("eu extingo o município de código IBGE {string}")
	public void eu_extingo_o_municipio(String codigoIbge) {
		extinguir(codigoIbge);
	}

	@Quando("eu tento extinguir o município de código IBGE {string}")
	public void eu_tento_extinguir_o_municipio(String codigoIbge) {
		extinguir(codigoIbge);
	}

	private void extinguir(String codigoIbge) {
		try {
			servico.extinguir(new MunicipioCodigoIbge(codigoIbge));
		} catch (RuntimeException excecao) {
			excecaoCapturada = excecao;
		}
	}

	@Entao("o município é incluído com sucesso")
	public void o_municipio_e_incluido_com_sucesso() {
		assertNull(excecaoCapturada);
		assertNotNull(resultadoInclusao);
	}

	@Entao("o município de código IBGE {string} passa a existir")
	public void o_municipio_passa_a_existir(String codigoIbge) {
		assertTrue(municipios.containsKey(codigoIbge));
	}

	@Entao("o município de código IBGE {string} continua a existir")
	public void o_municipio_continua_a_existir(String codigoIbge) {
		assertTrue(municipios.containsKey(codigoIbge));
	}

	@Entao("a inclusão do município é rejeitada por falta de autorização")
	public void a_inclusao_e_rejeitada_por_falta_de_autorizacao() {
		assertInstanceOf(NaoAutorizadoExcecao.class, excecaoCapturada);
	}

	@Entao("a inclusão é rejeitada porque já existe um município com o identificador {string}")
	public void a_inclusao_e_rejeitada_pelo_identificador(String codigoIbge) {
		assertMensagemDeExistencia(MunicipioServico.IDENTIFICADOR_EXISTENTE_MODELO, codigoIbge);
	}

	@Entao("a inclusão é rejeitada porque já existe um município com o nome {string} na unidade federativa")
	public void a_inclusao_e_rejeitada_pelo_nome(String nome) {
		assertMensagemDeExistencia(MunicipioServico.NOME_EXISTENTE_MODELO, nome);
	}

	@Entao("a inclusão é rejeitada porque não existe unidade federativa com o identificador {string}")
	public void a_inclusao_e_rejeitada_por_unidade_federativa_inexistente(String codigoIbge) {
		assertInstanceOf(AgregadoInexistenteExcecao.class, excecaoCapturada);
		assertEquals(String.format(UnidadeFederativaServico.IDENTIFICADOR_INEXISTENTE_MODELO, codigoIbge),
				excecaoCapturada.getMessage());
	}

	@Entao("o município encontrado tem nome {string}")
	public void o_municipio_encontrado_tem(String nome) {
		assertEquals(nome, resultadoBusca.getNome());
	}

	@Entao("a busca é rejeitada porque não existe município com o identificador {string}")
	public void a_busca_e_rejeitada_por_inexistencia(String codigoIbge) {
		assertMensagemDeInexistencia(codigoIbge);
	}

	@Entao("o município é alterado com sucesso")
	public void o_municipio_e_alterado_com_sucesso() {
		assertNull(excecaoCapturada);
	}

	@Entao("o município de código IBGE {string} passa a ter nome {string}")
	public void o_municipio_passa_a_ter(String codigoIbge, String nome) {
		var municipio = municipios.get(codigoIbge);
		assertNotNull(municipio);
		assertEquals(nome, municipio.getNome());
	}

	@Entao("a alteração do município é rejeitada por falta de autorização")
	public void a_alteracao_e_rejeitada_por_falta_de_autorizacao() {
		assertInstanceOf(NaoAutorizadoExcecao.class, excecaoCapturada);
	}

	@Entao("a alteração é rejeitada porque não existe município com o identificador {string}")
	public void a_alteracao_e_rejeitada_por_inexistencia(String codigoIbge) {
		assertMensagemDeInexistencia(codigoIbge);
	}

	@Entao("a inclusão é rejeitada porque o município só pode ser extinto pela ação de extinção")
	public void a_inclusao_e_rejeitada_por_extincao_indevida() {
		assertMensagemDeExtincaoIndevida();
	}

	@Entao("a alteração é rejeitada porque o município só pode ser extinto pela ação de extinção")
	public void a_alteracao_e_rejeitada_por_extincao_indevida() {
		assertMensagemDeExtincaoIndevida();
	}

	@Entao("a alteração é rejeitada porque o município {string} está extinto")
	public void a_alteracao_e_rejeitada_por_extincao(String codigoIbge) {
		assertMensagemDeExtincao(codigoIbge);
	}

	@Entao("o município é excluído com sucesso")
	public void o_municipio_e_excluido_com_sucesso() {
		assertNull(excecaoCapturada);
	}

	@Entao("o município de código IBGE {string} deixa de existir")
	public void o_municipio_deixa_de_existir(String codigoIbge) {
		assertFalse(municipios.containsKey(codigoIbge));
	}

	@Entao("a exclusão do município é rejeitada por falta de autorização")
	public void a_exclusao_e_rejeitada_por_falta_de_autorizacao() {
		assertInstanceOf(NaoAutorizadoExcecao.class, excecaoCapturada);
	}

	@Entao("a exclusão é rejeitada porque não existe município com o identificador {string}")
	public void a_exclusao_e_rejeitada_por_inexistencia(String codigoIbge) {
		assertMensagemDeInexistencia(codigoIbge);
	}

	@Entao("a exclusão é rejeitada porque o município {string} é referenciado")
	public void a_exclusao_e_rejeitada_por_referencia(String codigoIbge) {
		assertInstanceOf(IllegalStateException.class, excecaoCapturada);
		assertEquals(String.format(MunicipioServico.REFERENCIADO_MODELO, codigoIbge), excecaoCapturada.getMessage());
	}

	@Entao("o município é extinguido com sucesso")
	public void o_municipio_e_extinguido_com_sucesso() {
		assertNull(excecaoCapturada);
	}

	@Entao("o município de código IBGE {string} passa a estar extinto")
	public void o_municipio_passa_a_estar_extinto(String codigoIbge) {
		var municipio = municipios.get(codigoIbge);
		assertNotNull(municipio);
		assertTrue(municipio.isExtinto());
	}

	@Entao("a extinção do município é rejeitada por falta de autorização")
	public void a_extincao_e_rejeitada_por_falta_de_autorizacao() {
		assertInstanceOf(NaoAutorizadoExcecao.class, excecaoCapturada);
	}

	@Entao("a extinção é rejeitada porque não existe município com o identificador {string}")
	public void a_extincao_e_rejeitada_por_inexistencia(String codigoIbge) {
		assertMensagemDeInexistencia(codigoIbge);
	}

	@Entao("a extinção é rejeitada porque o município {string} está extinto")
	public void a_extincao_e_rejeitada_por_extincao(String codigoIbge) {
		assertMensagemDeExtincao(codigoIbge);
	}

	private void assertMensagemDeExistencia(String modelo, Object argumento) {
		assertInstanceOf(AgregadoExistenteExcecao.class, excecaoCapturada);
		assertEquals(String.format(modelo, argumento), excecaoCapturada.getMessage());
	}

	private void assertMensagemDeInexistencia(Object argumento) {
		assertInstanceOf(AgregadoInexistenteExcecao.class, excecaoCapturada);
		assertEquals(String.format(MunicipioServico.IDENTIFICADOR_INEXISTENTE_MODELO, argumento),
				excecaoCapturada.getMessage());
	}

	private void assertMensagemDeExtincaoIndevida() {
		assertInstanceOf(IllegalArgumentException.class, excecaoCapturada);
		assertEquals(MunicipioServico.EXTINCAO_INDEVIDA, excecaoCapturada.getMessage());
	}

	private void assertMensagemDeExtincao(Object argumento) {
		assertInstanceOf(IllegalStateException.class, excecaoCapturada);
		assertEquals(String.format(Municipio.EXTINTO_MODELO, argumento), excecaoCapturada.getMessage());
	}
}
