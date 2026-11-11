package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.planodetreino;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoServico;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(classes = BackendAplicacao.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:treinos;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "treino.reservas.agendamento=false"})
class TreinosIntegracaoTest {
    private static final String PLANOS = "/backend/core/plano-de-treino/";
    private static final String EXECUCOES = "/backend/core/execucao-de-exercicio/";

    @Autowired
    private WebApplicationContext contexto;
    @Autowired
    private JdbcTemplate banco;
    @Autowired
    private PasswordEncoder codificador;
    private MockMvc mvc;
    private final JsonMapper json = new JsonMapper();
    private long alunoId;
    private long exercicioId;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        for (var tabela : new String[] {"EXC_EXECUCAO_EXERCICIO", "SRE_SERIE", "TRE_TREINO", "PLT_PLANO_TREINO",
                "PCD_CREDENCIAL_PERSONAL", "PER_PERSONAL", "EXE_EXERCICIO", "USU_CREDENCIAL", "ALU_ALUNO"}) {
            banco.update("delete from " + tabela);
        }
        banco.update("insert into ALU_ALUNO(ALU_USUARIO,ALU_NOME) values ('u','Mariana'),('v','Pedro')");
        banco.update("insert into USU_CREDENCIAL(ALU_USUARIO,USU_SENHA) values ('u',?)", codificador.encode("u"));
        banco.update("insert into PER_PERSONAL(PER_USUARIO,PER_NOME) values ('p','Rafael'),('q','Camila')");
        banco.update("insert into PCD_CREDENCIAL_PERSONAL(PER_ID,PCD_SENHA) select PER_ID,? from PER_PERSONAL where PER_USUARIO = 'p'",
                codificador.encode("senha-personal"));
        banco.update("insert into EXE_EXERCICIO(EXE_NOME,EXE_CATEGORIA,EXE_CARGA_MAXIMA) values ('Supino','PEITO',100.00)");
        alunoId = banco.queryForObject("select ALU_ID from ALU_ALUNO where ALU_USUARIO = 'u'", Long.class);
        exercicioId = banco.queryForObject("select EXE_ID from EXE_EXERCICIO", Long.class);
    }

    private static UserRequestPostProcessor personal(String usuario) {
        return user(usuario).authorities(new SimpleGrantedAuthority("PERSONAL"));
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

    private String treinos(int series, int maximo) {
        return """
                [{"nome":"Treino A","dia":"SEGUNDA","objetivo":"HIPERTROFIA","series":[
                {"exercicio":"%d","quantidade":%d,"repeticoesMinimas":8,"repeticoesMaximas":%d,"carga":40,"intervaloSegundos":60}]}]
                """.formatted(exercicioId, series, maximo);
    }

    private String requisicaoPlano() {
        return "{\"aluno\":\"%d\",\"nome\":\"Hipertrofia\",\"frequenciaSemanal\":1,\"treinos\":%s}".formatted(alunoId, treinos(4, 12));
    }

    private String criarPlano() throws Exception {
        return sucesso(enviar(PLANOS + "criar", personal("p"), requisicaoPlano())).path("plano").path("identificador").asString();
    }

    private String criarPlanoAtivo() throws Exception {
        var plano = criarPlano();
        sucesso(enviar(PLANOS + plano + "/ativar", personal("p"), ""));
        return plano;
    }

    private MvcResult registrar(String plano, int repeticoes, String carga) throws Exception {
        return enviar(EXECUCOES + "registrar", aluno("u"),
                "{\"plano\":\"%s\",\"dia\":\"SEGUNDA\",\"exercicio\":\"%d\",\"series\":4,\"repeticoes\":%d,\"carga\":%s}"
                        .formatted(plano, exercicioId, repeticoes, carga));
    }

    private String mensagem(MvcResult resultado) throws Exception {
        return corpo(resultado).path("mensagem").asString();
    }

    @Test
    void loginDePersonalRespondeAutorizacoesDoMenu() throws Exception {
        var resposta = sucesso(mvc.perform(get("/backend/autenticacao").with(httpBasic("p", "senha-personal"))).andReturn());
        assertEquals("p", resposta.path("usuario").asString());
        assertTrue(resposta.path("aluno").path("buscar").asBoolean());
        assertTrue(resposta.path("planoDeTreino").path("criar").asBoolean());
        assertFalse(resposta.path("execucaoDeExercicio").path("registrar").asBoolean());
        assertFalse(resposta.path("armario").path("buscar").asBoolean());
    }

    @Test
    void loginDeAlunoNaoEnxergaListaDeAlunos() throws Exception {
        var resposta = sucesso(mvc.perform(get("/backend/autenticacao").with(httpBasic("u", "u"))).andReturn());
        assertFalse(resposta.path("aluno").path("buscar").asBoolean());
        assertTrue(resposta.path("execucaoDeExercicio").path("registrar").asBoolean());
        assertEquals(403, consultar("/backend/core/aluno/pesquisa", aluno("u")).getResponse().getStatus());
    }

    @Test
    void personalListaAlunosEFormulario() throws Exception {
        var alunos = sucesso(consultar("/backend/core/aluno/pesquisa?ordenacao=NOME", personal("p")));
        assertEquals(2, alunos.path("resultado").path("total").asInt());
        var formulario = sucesso(consultar(PLANOS + "inclusao", personal("p")));
        assertEquals("Supino", formulario.path("exercicios").get(0).path("nome").asString());
        assertEquals(3, formulario.path("objetivos").size());
        assertEquals(7, formulario.path("dias").size());
    }

    @Test
    void criaAtivaEExibePlanoParaOAluno() throws Exception {
        var plano = criarPlanoAtivo();
        var detalhe = sucesso(consultar(PLANOS + plano + "/visualizacao", aluno("u")));
        assertEquals("ATIVO", detalhe.path("plano").path("situacao").asString());
        assertEquals("Supino", detalhe.path("treinos").get(0).path("series").get(0).path("exercicioNome").asString());
        assertFalse(detalhe.path("gerenciavel").asBoolean());
        assertTrue(sucesso(consultar(PLANOS + plano + "/visualizacao", personal("p"))).path("gerenciavel").asBoolean());
    }

    @Test
    void exercicioRepetidoInformaOMotivo() throws Exception {
        var repetido = """
                [{"nome":"A","dia":"SEGUNDA","objetivo":"HIPERTROFIA","series":[
                {"exercicio":"%1$d","quantidade":4,"repeticoesMinimas":8,"repeticoesMaximas":12,"carga":40,"intervaloSegundos":60},
                {"exercicio":"%1$d","quantidade":3,"repeticoesMinimas":8,"repeticoesMaximas":12,"carga":30,"intervaloSegundos":60}]}]
                """.formatted(exercicioId);
        var resultado = enviar(PLANOS + "criar", personal("p"),
                "{\"aluno\":\"%d\",\"nome\":\"X\",\"frequenciaSemanal\":1,\"treinos\":%s}".formatted(alunoId, repetido));
        assertEquals(422, resultado.getResponse().getStatus());
        assertEquals(PlanoDeTreinoServico.EXERCICIO_REPETIDO_MODELO.formatted("Supino", "A"), mensagem(resultado));
    }

    @Test
    void alteracaoRespondeOFormularioNaMesmaOperacao() throws Exception {
        var plano = criarPlanoAtivo();
        var recurso = sucesso(consultar(PLANOS + plano + "/alteracao", personal("p")));
        assertEquals(plano, recurso.path("agregado").path("identificador").asString());
        assertTrue(recurso.path("autorizacoes").path("revisar").asBoolean());
        assertEquals(String.valueOf(exercicioId), recurso.path("formulario").path("exercicios").get(0).path("identificador").asString());
    }

    @Test
    void segundoPlanoAtivoRejeitadoESubstituicaoAceita() throws Exception {
        var primeiro = criarPlanoAtivo();
        var segundo = criarPlano();
        var rejeicao = enviar(PLANOS + segundo + "/ativar", personal("p"), "");
        assertEquals(409, rejeicao.getResponse().getStatus());
        assertEquals(PlanoDeTreinoServico.PLANO_ATIVO_EXISTENTE, mensagem(rejeicao));

        sucesso(enviar(PLANOS + segundo + "/substituir", personal("p"), ""));

        assertEquals("SUBSTITUIDO", sucesso(consultar(PLANOS + primeiro + "/visualizacao", personal("p"))).path("plano").path("situacao").asString());
        assertEquals("ATIVO", sucesso(consultar(PLANOS + segundo + "/visualizacao", personal("p"))).path("plano").path("situacao").asString());
    }

    @Test
    void alteracaoCriaVersaoSemMudarExecucoes() throws Exception {
        var plano = criarPlanoAtivo();
        assertEquals(200, registrar(plano, 10, "40").getResponse().getStatus());

        var nova = sucesso(enviar(PLANOS + plano + "/revisar", personal("p"),
                "{\"nome\":\"Versão 2\",\"frequenciaSemanal\":1,\"treinos\":%s}".formatted(treinos(5, 12))));

        var novoId = nova.path("plano").path("identificador").asString();
        assertFalse(plano.equals(novoId));
        assertEquals(2, nova.path("plano").path("versao").asInt());
        assertEquals(plano, nova.path("planoAnterior").asString());
        assertEquals("SUBSTITUIDO", sucesso(consultar(PLANOS + plano + "/visualizacao", personal("p"))).path("plano").path("situacao").asString());
        assertEquals(1, banco.queryForObject("select count(*) from EXC_EXECUCAO_EXERCICIO where PLT_ID = ?", Long.class, Long.valueOf(plano)));
    }

    @Test
    void outroPersonalNaoEncerraNemAltera() throws Exception {
        var plano = criarPlanoAtivo();
        var encerramento = enviar(PLANOS + plano + "/encerrar", personal("q"), "");
        assertEquals(403, encerramento.getResponse().getStatus());
        assertEquals(PlanoDeTreinoServico.PLANO_DE_OUTRO_PERSONAL, mensagem(encerramento));
        var revisao = enviar(PLANOS + plano + "/revisar", personal("q"),
                "{\"nome\":\"X\",\"frequenciaSemanal\":1,\"treinos\":%s}".formatted(treinos(4, 12)));
        assertEquals(403, revisao.getResponse().getStatus());
    }

    @Test
    void outroAlunoNaoConsultaNemRegistra() throws Exception {
        var plano = criarPlanoAtivo();
        assertEquals(403, consultar(PLANOS + plano + "/visualizacao", aluno("v")).getResponse().getStatus());
        var registro = enviar(EXECUCOES + "registrar", aluno("v"),
                "{\"plano\":\"%s\",\"dia\":\"SEGUNDA\",\"exercicio\":\"%d\",\"series\":4,\"repeticoes\":10,\"carga\":40}".formatted(plano, exercicioId));
        assertEquals(403, registro.getResponse().getStatus());
        assertEquals(403, consultar(EXECUCOES + "evolucao?aluno=" + alunoId, aluno("v")).getResponse().getStatus());
    }

    @Test
    void comandosGenericosDoD5ContinuamBloqueados() throws Exception {
        var plano = criarPlano();
        assertEquals(403, enviar(PLANOS + plano + "/excluir", personal("p"), "").getResponse().getStatus());
    }

    @Test
    void registroGeraSugestaoEEvolucaoCronologica() throws Exception {
        var plano = criarPlanoAtivo();
        var primeira = sucesso(registrar(plano, 12, "40"));
        assertTrue(primeira.path("sugestao").isNull());

        var segunda = sucesso(registrar(plano, 12, "40"));
        assertEquals(42.0, segunda.path("sugestao").path("cargaSugerida").asDouble());
        assertFalse(segunda.path("sugestao").path("progressaoMaximaAtingida").asBoolean());

        var evolucao = sucesso(consultar(EXECUCOES + "evolucao", aluno("u")));
        assertEquals(2, evolucao.path("execucoes").size());
        assertEquals(40.0, evolucao.path("maiorCarga").asDouble());
        assertEquals("Supino", evolucao.path("exercicios").get(0).path("nome").asString());

        var historico = sucesso(consultar(EXECUCOES + "pesquisa?ordenacao=DATA&descendente=true&aluno=" + alunoId, personal("p")));
        assertEquals(2, historico.path("resultado").path("total").asInt());
    }

    @Test
    void execucaoAcimaDoPlanoEPlanoEncerradoSaoRejeitados() throws Exception {
        var plano = criarPlanoAtivo();
        assertEquals(422, registrar(plano, 13, "40").getResponse().getStatus());

        sucesso(enviar(PLANOS + plano + "/encerrar", personal("p"), ""));

        var rejeicao = registrar(plano, 10, "40");
        assertEquals(409, rejeicao.getResponse().getStatus());
        assertEquals(PlanoDeTreinoServico.PLANO_ENCERRADO, mensagem(rejeicao));
        assertEquals("ENCERRADO", sucesso(consultar(PLANOS + plano + "/visualizacao", aluno("u"))).path("plano").path("situacao").asString());
    }

    @Test
    void telaDeRegistroTrazPlanoAtivoDoAluno() throws Exception {
        assertEquals(409, consultar(EXECUCOES + "inclusao", aluno("u")).getResponse().getStatus());
        var plano = criarPlanoAtivo();
        assertEquals(plano, sucesso(consultar(EXECUCOES + "inclusao", aluno("u"))).path("plano").path("identificador").asString());
    }

    @Test
    void telaDeAlteracaoRespondeAgregadoEAutorizacoes() throws Exception {
        var plano = criarPlano();
        var resposta = sucesso(consultar(PLANOS + plano + "/alteracao", personal("p")));
        assertEquals("Hipertrofia", resposta.path("agregado").path("nome").asString());
        assertEquals(1, resposta.path("agregado").path("treinos").size());
        assertTrue(resposta.path("autorizacoes").path("revisar").asBoolean());
    }
}
