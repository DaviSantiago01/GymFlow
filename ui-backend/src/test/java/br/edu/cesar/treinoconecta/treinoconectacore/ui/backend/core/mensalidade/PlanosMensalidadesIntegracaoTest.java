package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.mensalidade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import br.edu.cesar.treinoconecta.treinoconectacore.BackendAplicacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.LimpezaBanco;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(classes = BackendAplicacao.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:planos-mensalidades;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "treino.reservas.agendamento=false", "treino.grupos.agendamento=false"})
class PlanosMensalidadesIntegracaoTest {
    private static final String PLANOS = "/backend/core/plano/";
    private static final String ADESOES = "/backend/core/adesao-plano/";
    private static final String MENSALIDADES = "/backend/core/mensalidade/";

    @Autowired
    private WebApplicationContext contexto;
    @Autowired
    private JdbcTemplate banco;
    @Autowired
    private PasswordEncoder codificador;
    private MockMvc mvc;
    private final JsonMapper json = new JsonMapper();
    private long mariana;
    private long pedro;

    @BeforeEach
    void preparar() {
        LimpezaBanco.limparDependentes(banco);
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        for (var tabela : new String[] {"PLA_PLANO", "ADM_ADMINISTRADOR", "USU_CREDENCIAL", "ALU_ALUNO"}) {
            banco.update("delete from " + tabela);
        }
        banco.update("insert into ALU_ALUNO(ALU_USUARIO,ALU_NOME) values ('u','Mariana'),('v','Pedro')");
        banco.update("insert into USU_CREDENCIAL(ALU_USUARIO,USU_SENHA) values ('u',?)", codificador.encode("u"));
        banco.update("insert into ADM_ADMINISTRADOR(ADM_USUARIO,ADM_NOME,ADM_SENHA) values ('admin','Administração',?)",
                codificador.encode("senha-admin"));
        mariana = banco.queryForObject("select ALU_ID from ALU_ALUNO where ALU_USUARIO = 'u'", Long.class);
        pedro = banco.queryForObject("select ALU_ID from ALU_ALUNO where ALU_USUARIO = 'v'", Long.class);
    }

    private static UserRequestPostProcessor administrador() {
        return user("admin").authorities(new SimpleGrantedAuthority("ADMINISTRADOR"));
    }

    private static UserRequestPostProcessor aluno(String usuario) {
        return user(usuario).authorities(new SimpleGrantedAuthority("USUARIO"));
    }

    private MvcResult enviar(String caminho, UserRequestPostProcessor usuario, String corpo) throws Exception {
        return mvc.perform(post(caminho).with(usuario).with(csrf()).contentType("application/json").content(corpo)).andReturn();
    }

    private MvcResult consultar(String caminho, UserRequestPostProcessor usuario) throws Exception {
        return mvc.perform(get(caminho).with(usuario)).andReturn();
    }

    private JsonNode corpo(MvcResult resultado) throws Exception {
        return json.readTree(resultado.getResponse().getContentAsString());
    }

    private JsonNode sucesso(MvcResult resultado) throws Exception {
        assertEquals(200, resultado.getResponse().getStatus(), resultado.getResponse().getContentAsString());
        return corpo(resultado);
    }

    private void falha(MvcResult resultado, int status, String mensagem) throws Exception {
        assertEquals(status, resultado.getResponse().getStatus(), resultado.getResponse().getContentAsString());
        assertEquals(mensagem, corpo(resultado).path("mensagem").asString());
    }

    private String cadastrarPlano(String nome, String valor, int duracao) throws Exception {
        var corpo = "{\"nome\":\"%s\",\"valor\":%s,\"duracaoMeses\":%d}".formatted(nome, valor, duracao);
        return sucesso(enviar(PLANOS + "cadastrar", administrador(), corpo)).path("identificador").asString();
    }

    private JsonNode aderir(long aluno, String plano) throws Exception {
        return sucesso(enviar(ADESOES + "aderir", administrador(), "{\"aluno\":\"%d\",\"plano\":\"%s\"}".formatted(aluno, plano)));
    }

    private static LocalDate hoje() {
        return LocalDate.now(ParametrosAcademia.FUSO);
    }

    @Test
    void loginDoAdministradorLiberaSomenteAAdministracao() throws Exception {
        var resposta = sucesso(mvc.perform(get("/backend/autenticacao").with(httpBasic("admin", "senha-admin"))).andReturn());
        assertEquals("admin", resposta.path("usuario").asString());
        assertTrue(resposta.path("plano").path("cadastrar").asBoolean());
        assertTrue(resposta.path("adesaoPlano").path("aderir").asBoolean());
        assertTrue(resposta.path("mensalidade").path("registrarPagamento").asBoolean());
        assertTrue(resposta.path("profissional").path("cadastrar").asBoolean());
        assertFalse(resposta.path("armario").path("buscar").asBoolean());
        assertFalse(resposta.path("planoDeTreino").path("criar").asBoolean());
    }

    @Test
    void loginDoAlunoSoConsultaOProprioPlano() throws Exception {
        var resposta = sucesso(mvc.perform(get("/backend/autenticacao").with(httpBasic("u", "u"))).andReturn());
        assertFalse(resposta.path("plano").path("buscar").asBoolean());
        assertTrue(resposta.path("adesaoPlano").path("buscar").asBoolean());
        assertFalse(resposta.path("adesaoPlano").path("aderir").asBoolean());
        assertTrue(resposta.path("mensalidade").path("buscar").asBoolean());
        assertFalse(resposta.path("mensalidade").path("registrarPagamento").asBoolean());
    }

    @Test
    void cadastroDePlanoValidaDadosEDuplicidade() throws Exception {
        var plano = cadastrarPlano("Mensal", "99.90", 1);
        var visualizacao = sucesso(consultar(PLANOS + plano + "/visualizacao", administrador()));
        assertEquals("ATIVO", visualizacao.path("situacao").asString());
        assertEquals(0, visualizacao.path("adesoesAtivas").asInt());

        falha(enviar(PLANOS + "cadastrar", administrador(), "{\"nome\":\"MENSAL\",\"valor\":80,\"duracaoMeses\":1}"), 409,
                "Já existe um plano ativo com este nome e esta duração");
        falha(enviar(PLANOS + "cadastrar", administrador(), "{\"nome\":\"Semestral\",\"valor\":0,\"duracaoMeses\":6}"), 422,
                "O valor do plano deve ser maior que zero e no máximo 99999.99");
        falha(enviar(PLANOS + "cadastrar", administrador(), "{\"nome\":\"Semestral\",\"valor\":500}"), 422,
                "A duração do plano deve ser de 1 a 12 meses");

        var editado = sucesso(enviar(PLANOS + plano + "/editar", administrador(),
                "{\"nome\":\"Mensal Plus\",\"valor\":119.90,\"duracaoMeses\":1}"));
        assertEquals("Mensal Plus", editado.path("nome").asString());
        assertEquals("INATIVO", sucesso(enviar(PLANOS + plano + "/inativar", administrador(), "")).path("situacao").asString());
        falha(enviar(PLANOS + plano + "/editar", administrador(), "{\"nome\":\"X1\",\"valor\":1,\"duracaoMeses\":1}"), 409,
                "Somente um plano ativo pode ser alterado");

        var pesquisa = sucesso(consultar(PLANOS + "pesquisa?situacao=INATIVO&nome=plus", administrador()));
        assertEquals(1, pesquisa.path("resultado").path("total").asInt());
    }

    @Test
    void somenteOAdministradorGerenciaPlanosEAsRotasGenericasFicamBloqueadas() throws Exception {
        assertEquals(403, enviar(PLANOS + "cadastrar", aluno("u"), "{\"nome\":\"Mensal\",\"valor\":99,\"duracaoMeses\":1}")
                .getResponse().getStatus());
        assertEquals(403, consultar(PLANOS + "pesquisa", aluno("u")).getResponse().getStatus());
        assertEquals(403, enviar(PLANOS + "1/excluir", administrador(), "").getResponse().getStatus());
        var semCsrf = mvc.perform(post(PLANOS + "cadastrar").with(administrador()).contentType("application/json")
                .content("{\"nome\":\"Mensal\",\"valor\":99,\"duracaoMeses\":1}")).andReturn();
        assertEquals(403, semCsrf.getResponse().getStatus());
        assertEquals(0, banco.queryForObject("select count(*) from PLA_PLANO", Long.class));
    }

    @Test
    void adesaoGeraAPrimeiraMensalidadeEImpedeSegundaAdesaoAtiva() throws Exception {
        var plano = cadastrarPlano("Mensal", "99.90", 1);
        var formulario = sucesso(consultar(ADESOES + "inclusao", administrador()));
        assertEquals(2, formulario.path("alunos").size());
        assertEquals(1, formulario.path("planos").size());

        var adesao = aderir(mariana, plano);
        assertEquals("ATIVA", adesao.path("situacao").asString());
        assertEquals(hoje().toString(), adesao.path("inicio").asString());
        falha(enviar(ADESOES + "aderir", administrador(), "{\"aluno\":\"%d\",\"plano\":\"%s\"}".formatted(mariana, plano)), 409,
                "O aluno já possui um plano ativo. Troque ou cancele o plano atual antes de aderir a outro");

        var visualizacao = sucesso(consultar(ADESOES + adesao.path("identificador").asString() + "/visualizacao", administrador()));
        var mensalidade = visualizacao.path("mensalidades").get(0);
        assertEquals(1, visualizacao.path("mensalidades").size());
        assertEquals(99.90, mensalidade.path("valor").asDouble());
        assertEquals(hoje().plusDays(5).toString(), mensalidade.path("vencimento").asString());
        assertEquals("ABERTA", mensalidade.path("situacao").asString());

        assertEquals(0, sucesso(enviar(MENSALIDADES + "gerar", administrador(), "")).path("geradas").asInt());
    }

    @Test
    void trocaECancelamentoPreservamOHistorico() throws Exception {
        var mensal = cadastrarPlano("Mensal", "99.90", 1);
        var trimestral = cadastrarPlano("Trimestral", "269.90", 3);
        var adesao = aderir(mariana, mensal).path("identificador").asString();
        falha(enviar(ADESOES + adesao + "/trocar", administrador(), "{\"plano\":\"%s\"}".formatted(mensal)), 409,
                "Escolha um plano diferente do plano atual");

        var nova = sucesso(enviar(ADESOES + adesao + "/trocar", administrador(), "{\"plano\":\"%s\"}".formatted(trimestral)));
        assertEquals("Trimestral", nova.path("planoNome").asString());
        var anterior = sucesso(consultar(ADESOES + adesao + "/visualizacao", administrador())).path("adesao");
        assertEquals("ENCERRADA", anterior.path("situacao").asString());
        assertEquals(hoje().toString(), anterior.path("encerramento").asString());

        var cancelada = sucesso(enviar(ADESOES + nova.path("identificador").asString() + "/cancelar", administrador(), ""));
        assertEquals("CANCELADA", cancelada.path("situacao").asString());
        assertEquals(2, banco.queryForObject("select count(*) from MEN_MENSALIDADE where ALU_ID = ?", Long.class, mariana));
        assertEquals(2, sucesso(consultar(ADESOES + "pesquisa?aluno=" + mariana, administrador()))
                .path("resultado").path("total").asInt());
    }

    @Test
    void pagamentoEEstornoAtualizamAMensalidade() throws Exception {
        var plano = cadastrarPlano("Mensal", "99.90", 1);
        aderir(mariana, plano);
        var mensalidade = banco.queryForObject("select MEN_ID from MEN_MENSALIDADE", Long.class);

        falha(enviar(MENSALIDADES + mensalidade + "/pagar", aluno("u"), "{\"forma\":\"PIX\"}"), 403, "Ação não autorizada");
        var paga = sucesso(enviar(MENSALIDADES + mensalidade + "/pagar", administrador(), "{\"forma\":\"PIX\"}"));
        assertEquals("PAGA", paga.path("mensalidade").path("situacao").asString());
        assertEquals("CONFIRMADO", paga.path("pagamentos").get(0).path("situacao").asString());
        falha(enviar(MENSALIDADES + mensalidade + "/pagar", administrador(), "{\"forma\":\"DINHEIRO\"}"), 409,
                "Somente mensalidades em aberto ou em atraso podem receber pagamento");

        var pagamento = paga.path("pagamentos").get(0).path("identificador").asLong();
        falha(enviar(MENSALIDADES + mensalidade + "/pagamentos/" + pagamento + "/estornar", administrador(), "{}"), 422,
                "Informe o motivo do estorno com até 250 caracteres");
        var estornada = sucesso(enviar(MENSALIDADES + mensalidade + "/pagamentos/" + pagamento + "/estornar", administrador(),
                "{\"motivo\":\"Pagamento lançado em duplicidade\"}"));
        assertEquals("ABERTA", estornada.path("mensalidade").path("situacao").asString());
        assertEquals("ESTORNADO", estornada.path("pagamentos").get(0).path("situacao").asString());
        assertEquals("Pagamento lançado em duplicidade", estornada.path("pagamentos").get(0).path("motivoEstorno").asString());
        falha(enviar(MENSALIDADES + mensalidade + "/pagamentos/" + pagamento + "/estornar", administrador(),
                "{\"motivo\":\"De novo\"}"), 409, "Este pagamento já foi estornado");
    }

    @Test
    void mensalidadeVencidaApareceAtrasadaComMultaEJuros() throws Exception {
        var plano = Long.valueOf(cadastrarPlano("Mensal", "99.90", 1));
        var inicio = hoje().minusDays(15);
        banco.update("insert into ADP_ADESAO_PLANO(ALU_ID,PLA_ID,ADP_PERIODICIDADE_MESES,ADP_INICIO,ADP_SITUACAO) values (?,?,1,?,'ATIVA')",
                pedro, plano, inicio);
        var adesao = banco.queryForObject("select ADP_ID from ADP_ADESAO_PLANO", Long.class);
        banco.update("insert into MEN_MENSALIDADE(ADP_ID,ALU_ID,MEN_PERIODO_INICIO,MEN_PERIODO_FIM,MEN_VENCIMENTO,MEN_VALOR,MEN_SITUACAO) values (?,?,?,?,?,99.90,'ABERTA')",
                adesao, pedro, inicio, inicio.plusMonths(1).minusDays(1), inicio.plusDays(5));

        var atrasadas = sucesso(consultar(MENSALIDADES + "pesquisa?situacao=ATRASADA", administrador())).path("resultado");
        assertEquals(1, atrasadas.path("total").asInt());
        var mensalidade = atrasadas.path("projecoes").get(0);
        assertEquals("ATRASADA", mensalidade.path("situacao").asString());
        assertEquals(10, mensalidade.path("diasAtraso").asInt());
        assertEquals(2.00, mensalidade.path("multa").asDouble());
        assertEquals(0.33, mensalidade.path("juros").asDouble());
        assertEquals(102.23, mensalidade.path("valorDevido").asDouble());
        assertEquals(0, sucesso(consultar(MENSALIDADES + "pesquisa?situacao=ABERTA", administrador()))
                .path("resultado").path("total").asInt());

        var paga = sucesso(enviar(MENSALIDADES + mensalidade.path("identificador").asString() + "/pagar", administrador(),
                "{\"forma\":\"DINHEIRO\"}"));
        assertEquals(102.23, paga.path("pagamentos").get(0).path("valor").asDouble());
    }

    @Test
    void alunoVeSomenteOProprioPlanoEAsPropriasMensalidades() throws Exception {
        var plano = cadastrarPlano("Mensal", "99.90", 1);
        aderir(mariana, plano);
        var adesaoPedro = aderir(pedro, plano).path("identificador").asString();

        var meuPlano = sucesso(consultar(ADESOES + "meu-plano", aluno("u")));
        assertEquals("Mariana", meuPlano.path("adesao").path("alunoNome").asString());
        assertEquals(1, meuPlano.path("mensalidades").size());
        assertEquals(1, sucesso(consultar(MENSALIDADES + "pesquisa", aluno("u"))).path("resultado").path("total").asInt());
        assertEquals(403, consultar(ADESOES + adesaoPedro + "/visualizacao", aluno("u")).getResponse().getStatus());
        assertEquals(403, consultar(ADESOES + "meu-plano", administrador()).getResponse().getStatus());
    }
}
