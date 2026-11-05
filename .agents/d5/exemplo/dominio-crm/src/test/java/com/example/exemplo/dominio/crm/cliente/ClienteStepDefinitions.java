package com.example.exemplo.dominio.crm.cliente;

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
import java.util.Map;
import java.util.Optional;

import com.example.exemplo.dominio.Autorizacao;
import com.example.exemplo.dominio.crm.cliente.spi.IClienteRepositorio;
import com.example.exemplo.dominio.geografia.municipio.Municipio;
import com.example.exemplo.dominio.geografia.municipio.MunicipioCodigoIbge;
import com.example.exemplo.dominio.geografia.municipio.MunicipioServico;
import com.example.exemplo.dominio.geografia.municipio.spi.IMunicipioRepositorio;

import dev.sauloaraujo.d5.framework.dominio.AgregadoExistenteExcecao;
import dev.sauloaraujo.d5.framework.dominio.AgregadoInexistenteExcecao;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

public final class ClienteStepDefinitions {
	private static final String MUNICIPIO_PADRAO = "3550308";

	private final Map<String, Cliente> clientes = new HashMap<>();
	private final Map<String, Municipio> municipios = new HashMap<>();
	private final ISegurancaServico segurancaServico = mock(ISegurancaServico.class);
	private final IClienteRepositorio repositorio = mock(IClienteRepositorio.class);
	private final IMunicipioRepositorio municipioRepositorio = mock(IMunicipioRepositorio.class);
	private final ClienteServico servico = new ClienteServico(segurancaServico, repositorio, municipioRepositorio);

	private int proximoIdentificador = 1;
	private Cliente resultadoInclusao;
	private Cliente resultadoBusca;
	private RuntimeException excecaoCapturada;

	public ClienteStepDefinitions() {
		when(municipioRepositorio.buscarPorIdentificador(any())).thenAnswer(chamada -> {
			MunicipioCodigoIbge codigoIbge = chamada.getArgument(0);
			var municipio = municipios.get(codigoIbge.toString());
			if (municipio == null) {
				throw new AgregadoInexistenteExcecao(
						String.format(MunicipioServico.IDENTIFICADOR_INEXISTENTE_MODELO, codigoIbge));
			}
			return municipio;
		});

		when(repositorio.existePorIdentificador(any()))
				.thenAnswer(chamada -> clientes.containsKey(chave(chamada.getArgument(0))));

		when(repositorio.buscarPorIdentificador(any())).thenAnswer(chamada -> {
			ClienteIdentificador identificador = chamada.getArgument(0);
			var cliente = clientes.get(chave(identificador));
			if (cliente == null) {
				throw new AgregadoInexistenteExcecao(
						String.format(ClienteServico.IDENTIFICADOR_INEXISTENTE_MODELO, identificador));
			}
			return cliente;
		});

		when(repositorio.existePorCpf(any(), any())).thenAnswer(chamada -> {
			Cpf cpf = chamada.getArgument(0);
			Optional<ClienteIdentificador> identificadorIgnorado = chamada.getArgument(1);
			return clientes.values().stream().anyMatch(
					candidato -> candidato.getCpf().equals(cpf) && !foiIgnorado(candidato, identificadorIgnorado));
		});

		when(repositorio.incluir(any())).thenAnswer(chamada -> {
			Cliente cliente = chamada.getArgument(0);
			var identificador = new ClienteIdentificador(String.valueOf(proximoIdentificador++));
			var incluido = new Cliente(Optional.of(identificador), cliente.getCpf(), cliente.getNome(),
					cliente.getEndereco());
			clientes.put(chave(identificador), incluido);
			return incluido;
		});

		doAnswer(chamada -> {
			Cliente cliente = chamada.getArgument(0);
			clientes.put(chave(cliente.getIdentificador().orElseThrow()), cliente);
			return null;
		}).when(repositorio).alterar(any());

		doAnswer(chamada -> {
			ClienteIdentificador identificador = chamada.getArgument(0);
			clientes.remove(chave(identificador));
			return null;
		}).when(repositorio).excluir(any());
	}

	private static String chave(ClienteIdentificador identificador) {
		return identificador.toString();
	}

	private static boolean foiIgnorado(Cliente candidato, Optional<ClienteIdentificador> identificadorIgnorado) {
		return identificadorIgnorado.isPresent() && candidato.getIdentificador().equals(identificadorIgnorado);
	}

	private static Cliente criar(Optional<String> identificador, String cpf, String nome, String municipio) {
		var endereco = new Endereco("Avenida Paulista", "1000", Optional.empty(), "Bela Vista",
				new EnderecoCep("01310100"), new MunicipioCodigoIbge(municipio));
		return new Cliente(identificador.map(ClienteIdentificador::new), new Cpf(cpf), nome, endereco);
	}

	private void adicionarMunicipio(String codigoIbge, boolean extinto) {
		var municipio = new Municipio(new MunicipioCodigoIbge(codigoIbge), "Município " + codigoIbge, Optional.empty());
		municipios.put(codigoIbge, extinto ? municipio.extinguir() : municipio);
	}

	@Dado("que não existe nenhum cliente cadastrado")
	public void que_nao_existe_nenhum_cliente_cadastrado() {
		clientes.clear();
		municipios.clear();
	}

	@Dado("que existe o município de código IBGE {string}")
	public void que_existe_o_municipio(String codigoIbge) {
		adicionarMunicipio(codigoIbge, false);
	}

	@Dado("que existe o município extinto de código IBGE {string}")
	public void que_existe_o_municipio_extinto(String codigoIbge) {
		adicionarMunicipio(codigoIbge, true);
	}

	@Dado("que o município de código IBGE {string} foi extinto")
	public void que_o_municipio_foi_extinto(String codigoIbge) {
		municipios.put(codigoIbge, municipios.get(codigoIbge).extinguir());
	}

	@Dado("que sou um administrador do cadastro de clientes")
	public void que_sou_um_administrador_do_cadastro_de_clientes() {
		when(segurancaServico.possuiAutorizacao(Autorizacao.ADMINISTRADOR.name())).thenReturn(true);
	}

	@Dado("que sou um usuário sem autorização para administrar clientes")
	public void que_sou_um_usuario_sem_autorizacao_para_administrar_clientes() {
		when(segurancaServico.possuiAutorizacao(Autorizacao.ADMINISTRADOR.name())).thenReturn(false);
	}

	@Dado("que existe um cliente com identificador {string}, CPF {string} e nome {string}")
	public void que_existe_um_cliente(String identificador, String cpf, String nome) {
		clientes.put(identificador, criar(Optional.of(identificador), cpf, nome, MUNICIPIO_PADRAO));
	}

	@Quando("eu incluo o cliente de CPF {string}, nome {string} e município {string}")
	public void eu_incluo_o_cliente(String cpf, String nome, String municipio) {
		incluir(cpf, nome, municipio);
	}

	@Quando("eu tento incluir o cliente de CPF {string}, nome {string} e município {string}")
	public void eu_tento_incluir_o_cliente(String cpf, String nome, String municipio) {
		incluir(cpf, nome, municipio);
	}

	@Quando("eu tento incluir o cliente de identificador {string}, CPF {string}, nome {string} e município {string}")
	public void eu_tento_incluir_o_cliente_com_identificador(String identificador, String cpf, String nome,
			String municipio) {
		incluir(Optional.of(identificador), cpf, nome, municipio);
	}

	private void incluir(String cpf, String nome, String municipio) {
		incluir(Optional.empty(), cpf, nome, municipio);
	}

	private void incluir(Optional<String> identificador, String cpf, String nome, String municipio) {
		try {
			resultadoInclusao = servico.incluir(criar(identificador, cpf, nome, municipio));
		} catch (RuntimeException excecao) {
			excecaoCapturada = excecao;
		}
	}

	@Quando("eu busco o cliente de identificador {string}")
	public void eu_busco_o_cliente(String identificador) {
		buscar(identificador);
	}

	@Quando("eu tento buscar o cliente de identificador {string}")
	public void eu_tento_buscar_o_cliente(String identificador) {
		buscar(identificador);
	}

	private void buscar(String identificador) {
		try {
			resultadoBusca = servico.buscar(new ClienteIdentificador(identificador));
		} catch (RuntimeException excecao) {
			excecaoCapturada = excecao;
		}
	}

	@Quando("eu altero o cliente de identificador {string} para CPF {string} e nome {string}")
	public void eu_altero_o_cliente(String identificador, String cpf, String nome) {
		alterar(identificador, cpf, nome);
	}

	@Quando("eu tento alterar o cliente de identificador {string} para CPF {string} e nome {string}")
	public void eu_tento_alterar_o_cliente(String identificador, String cpf, String nome) {
		alterar(identificador, cpf, nome);
	}

	@Quando("eu altero o cliente de identificador {string} para CPF {string}, nome {string} e município {string}")
	public void eu_altero_o_cliente_e_o_municipio(String identificador, String cpf, String nome, String municipio) {
		alterar(identificador, cpf, nome, municipio);
	}

	@Quando("eu tento alterar o cliente de identificador {string} para CPF {string}, nome {string} e município {string}")
	public void eu_tento_alterar_o_cliente_e_o_municipio(String identificador, String cpf, String nome,
			String municipio) {
		alterar(identificador, cpf, nome, municipio);
	}

	private void alterar(String identificador, String cpf, String nome) {
		alterar(identificador, cpf, nome, MUNICIPIO_PADRAO);
	}

	private void alterar(String identificador, String cpf, String nome, String municipio) {
		try {
			servico.alterar(criar(Optional.of(identificador), cpf, nome, municipio));
		} catch (RuntimeException excecao) {
			excecaoCapturada = excecao;
		}
	}

	@Quando("eu excluo o cliente de identificador {string}")
	public void eu_excluo_o_cliente(String identificador) {
		excluir(identificador);
	}

	@Quando("eu tento excluir o cliente de identificador {string}")
	public void eu_tento_excluir_o_cliente(String identificador) {
		excluir(identificador);
	}

	private void excluir(String identificador) {
		try {
			servico.excluir(new ClienteIdentificador(identificador));
		} catch (RuntimeException excecao) {
			excecaoCapturada = excecao;
		}
	}

	@Entao("o cliente é incluído com sucesso")
	public void o_cliente_e_incluido_com_sucesso() {
		assertNull(excecaoCapturada);
		assertNotNull(resultadoInclusao);
		assertTrue(resultadoInclusao.getIdentificador().isPresent());
	}

	@Entao("o cliente de CPF {string} passa a existir")
	public void o_cliente_passa_a_existir(String cpf) {
		assertTrue(clientes.values().stream().anyMatch(cliente -> cliente.getCpf().equals(new Cpf(cpf))));
	}

	@Entao("a inclusão do cliente é rejeitada por falta de autorização")
	public void a_inclusao_e_rejeitada_por_falta_de_autorizacao() {
		assertInstanceOf(NaoAutorizadoExcecao.class, excecaoCapturada);
	}

	@Entao("a inclusão é rejeitada porque o identificador do cliente é gerado na inclusão")
	public void a_inclusao_e_rejeitada_pelo_identificador_informado() {
		assertInstanceOf(IllegalArgumentException.class, excecaoCapturada);
		assertEquals(ClienteServico.IDENTIFICADOR_INFORMADO, excecaoCapturada.getMessage());
	}

	@Entao("a inclusão é rejeitada porque já existe um cliente com o CPF {string}")
	public void a_inclusao_e_rejeitada_pelo_cpf(String cpf) {
		assertMensagemDeCpfExistente(cpf);
	}

	@Entao("a inclusão é rejeitada porque não existe município com o identificador {string}")
	public void a_inclusao_e_rejeitada_por_municipio_inexistente(String codigoIbge) {
		assertMensagemDeMunicipioInexistente(codigoIbge);
	}

	@Entao("a inclusão é rejeitada porque o município {string} está extinto")
	public void a_inclusao_e_rejeitada_por_municipio_extinto(String codigoIbge) {
		assertMensagemDeMunicipioExtinto(codigoIbge);
	}

	@Entao("o cliente encontrado tem CPF {string} e nome {string}")
	public void o_cliente_encontrado_tem(String cpf, String nome) {
		assertEquals(new Cpf(cpf), resultadoBusca.getCpf());
		assertEquals(nome, resultadoBusca.getNome());
	}

	@Entao("a busca é rejeitada porque não existe cliente com o identificador {string}")
	public void a_busca_e_rejeitada_por_inexistencia(String identificador) {
		assertMensagemDeInexistencia(identificador);
	}

	@Entao("o cliente é alterado com sucesso")
	public void o_cliente_e_alterado_com_sucesso() {
		assertNull(excecaoCapturada);
	}

	@Entao("o cliente de identificador {string} passa a ter CPF {string} e nome {string}")
	public void o_cliente_passa_a_ter(String identificador, String cpf, String nome) {
		var cliente = clientes.get(identificador);
		assertNotNull(cliente);
		assertEquals(new Cpf(cpf), cliente.getCpf());
		assertEquals(nome, cliente.getNome());
	}

	@Entao("o cliente de identificador {string} passa a ter o município {string}")
	public void o_cliente_passa_a_ter_o_municipio(String identificador, String municipio) {
		var cliente = clientes.get(identificador);
		assertNotNull(cliente);
		assertEquals(new MunicipioCodigoIbge(municipio), cliente.getEndereco().getMunicipio());
	}

	@Entao("a alteração do cliente é rejeitada por falta de autorização")
	public void a_alteracao_e_rejeitada_por_falta_de_autorizacao() {
		assertInstanceOf(NaoAutorizadoExcecao.class, excecaoCapturada);
	}

	@Entao("a alteração é rejeitada porque não existe cliente com o identificador {string}")
	public void a_alteracao_e_rejeitada_por_inexistencia(String identificador) {
		assertMensagemDeInexistencia(identificador);
	}

	@Entao("a alteração é rejeitada porque já existe um cliente com o CPF {string}")
	public void a_alteracao_e_rejeitada_pelo_cpf(String cpf) {
		assertMensagemDeCpfExistente(cpf);
	}

	@Entao("a alteração é rejeitada porque não existe município com o identificador {string}")
	public void a_alteracao_e_rejeitada_por_municipio_inexistente(String codigoIbge) {
		assertMensagemDeMunicipioInexistente(codigoIbge);
	}

	@Entao("a alteração é rejeitada porque o município {string} está extinto")
	public void a_alteracao_e_rejeitada_por_municipio_extinto(String codigoIbge) {
		assertMensagemDeMunicipioExtinto(codigoIbge);
	}

	@Entao("o cliente é excluído com sucesso")
	public void o_cliente_e_excluido_com_sucesso() {
		assertNull(excecaoCapturada);
	}

	@Entao("o cliente de identificador {string} deixa de existir")
	public void o_cliente_deixa_de_existir(String identificador) {
		assertFalse(clientes.containsKey(identificador));
	}

	@Entao("a exclusão do cliente é rejeitada por falta de autorização")
	public void a_exclusao_e_rejeitada_por_falta_de_autorizacao() {
		assertInstanceOf(NaoAutorizadoExcecao.class, excecaoCapturada);
	}

	@Entao("a exclusão é rejeitada porque não existe cliente com o identificador {string}")
	public void a_exclusao_e_rejeitada_por_inexistencia(String identificador) {
		assertMensagemDeInexistencia(identificador);
	}

	private void assertMensagemDeCpfExistente(String cpf) {
		assertInstanceOf(AgregadoExistenteExcecao.class, excecaoCapturada);
		assertEquals(String.format(ClienteServico.CPF_EXISTENTE_MODELO, cpf), excecaoCapturada.getMessage());
	}

	private void assertMensagemDeInexistencia(String identificador) {
		assertInstanceOf(AgregadoInexistenteExcecao.class, excecaoCapturada);
		assertEquals(String.format(ClienteServico.IDENTIFICADOR_INEXISTENTE_MODELO, identificador),
				excecaoCapturada.getMessage());
	}

	private void assertMensagemDeMunicipioInexistente(String codigoIbge) {
		assertInstanceOf(AgregadoInexistenteExcecao.class, excecaoCapturada);
		assertEquals(String.format(MunicipioServico.IDENTIFICADOR_INEXISTENTE_MODELO, codigoIbge),
				excecaoCapturada.getMessage());
	}

	private void assertMensagemDeMunicipioExtinto(String codigoIbge) {
		assertInstanceOf(IllegalStateException.class, excecaoCapturada);
		assertEquals(String.format(Municipio.EXTINTO_MODELO, codigoIbge), excecaoCapturada.getMessage());
	}
}
