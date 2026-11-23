package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.adesaoplano;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
        "spring.datasource.url=jdbc:h2:mem:recorrencia-planos;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "treino.reservas.agendamento=false", "treino.grupos.agendamento=false", "treino.adesoes.agendamento=false"})
class RecorrenciaPlanosIntegracaoTest {
    private static final String PLANOS = "/backend/core/plano/";
    private static final String ADESOES = "/backend/core/adesao-plano/";
    private static final String MENSALIDADES = "/backend/core/mensalidade/";

    @Autowired
    private WebApplicationContext contexto;
    @Autowired
    private JdbcTemplate banco;
    private MockMvc mvc;
    private final JsonMapper json = new JsonMapper();
    private long mariana;
    private String mensal;

    @BeforeEach
    void preparar() throws Exception {
        LimpezaBanco.limparDependentes(banco);
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        for (var tabela : new String[] {"PLA_PLANO", "USU_CREDENCIAL", "ALU_ALUNO"}) {
            banco.update("delete from " + tabela);
        }
        banco.update("insert into ALU_ALUNO(ALU_USUARIO,ALU_NOME) values ('u','Mariana')");
        mariana = banco.queryForObject("select ALU_ID from ALU_ALUNO where ALU_USUARIO = 'u'", Long.class);
        mensal = cadastrarPlano("Mensal", "99.90", 1);
    }

    private static UserRequestPostProcessor administrador() {
        return user("admin").authorities(new SimpleGrantedAuthority("ADMINISTRADOR"));
    }

    private static UserRequestPostProcessor aluno() {
        return user("u").authorities(new SimpleGrantedAuthority("USUARIO"));
    }

    private MvcResult enviar(String caminho, UserRequestPostProcessor usuario, String corpo) throws Exception {
        return mvc.perform(post(caminho).with(usuario).with(csrf()).contentType("application/json").content(corpo)).andReturn();
    }

    private JsonNode sucesso(MvcResult resultado) throws Exception {
        assertEquals(200, resultado.getResponse().getStatus(), resultado.getResponse().getContentAsString());
        return json.readTree(resultado.getResponse().getContentAsString());
    }

    private JsonNode consultar(String caminho, UserRequestPostProcessor usuario) throws Exception {
        return sucesso(mvc.perform(get(caminho).with(usuario)).andReturn());
    }

    private String cadastrarPlano(String nome, String valor, int duracao) throws Exception {
        var corpo = "{\"nome\":\"%s\",\"valor\":%s,\"duracaoMeses\":%d}".formatted(nome, valor, duracao);
        return sucesso(enviar(PLANOS + "cadastrar", administrador(), corpo)).path("identificador").asString();
    }

    private MvcResult aderir(String corpoRecorrencia) throws Exception {
        return enviar(ADESOES + "aderir", administrador(),
                "{\"aluno\":\"%d\",\"plano\":\"%s\"%s}".formatted(mariana, mensal, corpoRecorrencia));
    }

    /** Adesão com início no passado, gravada direto no banco, para simular o tempo decorrido. */
    private long adesaoNoPassado(LocalDate inicio, int quantidade) {
        banco.update("""
                insert into ADP_ADESAO_PLANO(ALU_ID,PLA_ID,ADP_PERIODICIDADE_MESES,ADP_INICIO,ADP_SITUACAO,ADP_QUANTIDADE_PERIODOS)
                values (?,?,1,?,'ATIVA',?)""", mariana, Long.valueOf(mensal), inicio, quantidade);
        return banco.queryForObject("select ADP_ID from ADP_ADESAO_PLANO where ALU_ID = ?", Long.class, mariana);
    }

    private static List<String> situacoes(JsonNode cronograma) {
        var situacoes = new ArrayList<String>();
        cronograma.path("parcelas").forEach(parcela -> situacoes.add(parcela.path("situacao").asString()));
        return situacoes;
    }

    private static LocalDate hoje() {
        return LocalDate.now(ParametrosAcademia.FUSO);
    }

    @Test
    void adesaoComRecorrenciaRegistraDuracaoEMostraTodasAsParcelas() throws Exception {
        var adesao = sucesso(aderir(",\"quantidadePeriodos\":5"));
        assertEquals(5, adesao.path("quantidadePeriodos").asInt());
        assertEquals(hoje().plusMonths(5).minusDays(1).toString(), adesao.path("terminoPrevisto").asString());

        var cronograma = consultar(ADESOES + adesao.path("identificador").asString() + "/visualizacao", administrador())
                .path("cronograma");
        assertEquals(5, cronograma.path("totalParcelas").asInt());
        assertEquals(List.of("ABERTA", "PREVISTA", "PREVISTA", "PREVISTA", "PREVISTA"), situacoes(cronograma));
        assertEquals(5, cronograma.path("restantes").asInt());
        assertEquals(499.50, cronograma.path("valorRestante").asDouble());
        var segunda = cronograma.path("parcelas").get(1);
        assertEquals(2, segunda.path("numero").asInt());
        assertEquals(hoje().plusMonths(1).plusDays(5).toString(), segunda.path("vencimento").asString());
        assertEquals(99.90, segunda.path("valor").asDouble());
    }

    @Test
    void alunoVeQuantasMensalidadesFaltamEAsQueJaPagou() throws Exception {
        sucesso(aderir(",\"quantidadePeriodos\":5"));
        var antes = consultar(ADESOES + "meu-plano", aluno()).path("cronograma");
        assertEquals(5, antes.path("restantes").asInt());
        assertEquals(0, antes.path("pagas").asInt());

        var mensalidade = banco.queryForObject("select MEN_ID from MEN_MENSALIDADE", Long.class);
        sucesso(enviar(MENSALIDADES + mensalidade + "/pagar", administrador(), "{\"forma\":\"PIX\"}"));
        var depois = consultar(ADESOES + "meu-plano", aluno());
        var cronograma = depois.path("cronograma");
        assertEquals(1, cronograma.path("pagas").asInt());
        assertEquals(4, cronograma.path("restantes").asInt());
        assertEquals(4, cronograma.path("previstas").asInt());
        assertEquals(399.60, cronograma.path("valorRestante").asDouble());
        assertEquals("PAGA", cronograma.path("parcelas").get(0).path("situacao").asString());
        assertEquals(5, depois.path("adesao").path("quantidadePeriodos").asInt());
    }

    @Test
    void cronogramaSeparaParcelasPagasAtrasadasEAVencer() throws Exception {
        var adesao = adesaoNoPassado(hoje().minusMonths(1).minusDays(10), 3);
        assertEquals(2, sucesso(enviar(MENSALIDADES + "gerar", administrador(), "")).path("geradas").asInt());
        var primeira = banco.queryForObject("select MEN_ID from MEN_MENSALIDADE where ADP_ID = ? order by MEN_PERIODO_INICIO "
                + "fetch first 1 row only", Long.class, adesao);
        sucesso(enviar(MENSALIDADES + primeira + "/pagar", administrador(), "{\"forma\":\"DINHEIRO\"}"));

        var cronograma = consultar(ADESOES + "meu-plano", aluno()).path("cronograma");
        assertEquals(List.of("PAGA", "ATRASADA", "PREVISTA"), situacoes(cronograma));
        assertEquals(1, cronograma.path("atrasadas").asInt());
        assertEquals(2, cronograma.path("restantes").asInt());
        assertTrue(cronograma.path("parcelas").get(1).path("valor").asDouble() > 99.90, "Parcela atrasada inclui multa e juros");
    }

    @Test
    void geracaoConcluiAAdesaoNoFimDaRecorrenciaELiberaNovoPlano() throws Exception {
        var inicio = hoje().minusMonths(3);
        var adesao = adesaoNoPassado(inicio, 2);
        assertEquals(2, sucesso(enviar(MENSALIDADES + "gerar", administrador(), "")).path("geradas").asInt());

        var visualizacao = consultar(ADESOES + adesao + "/visualizacao", administrador()).path("adesao");
        assertEquals("CONCLUIDA", visualizacao.path("situacao").asString());
        assertEquals(inicio.plusMonths(2).toString(), visualizacao.path("encerramento").asString());
        assertEquals(2, banco.queryForObject("select count(*) from MEN_MENSALIDADE where ADP_ID = ?", Long.class, adesao));
        assertEquals("ATIVA", sucesso(aderir("")).path("situacao").asString());
    }

    @Test
    void semRecorrenciaACobrancaContinuaComoAntes() throws Exception {
        var adesao = sucesso(aderir(""));
        assertTrue(adesao.path("quantidadePeriodos").isNull());
        assertTrue(adesao.path("terminoPrevisto").isNull());
        var cronograma = consultar(ADESOES + "meu-plano", aluno()).path("cronograma");
        assertTrue(cronograma.path("totalParcelas").isNull());
        assertEquals(List.of("ABERTA"), situacoes(cronograma));
        assertEquals(1, cronograma.path("restantes").asInt());
    }

    @Test
    void trocaPodeDefinirARecorrenciaDoNovoPlano() throws Exception {
        var trimestral = cadastrarPlano("Trimestral", "269.90", 3);
        var atual = sucesso(aderir("")).path("identificador").asString();
        var nova = sucesso(enviar(ADESOES + atual + "/trocar", administrador(),
                "{\"plano\":\"%s\",\"quantidadePeriodos\":2}".formatted(trimestral)));
        assertEquals(2, nova.path("quantidadePeriodos").asInt());
        assertEquals(hoje().plusMonths(6).minusDays(1).toString(), nova.path("terminoPrevisto").asString());
    }

    @Test
    void recorrenciaForaDoIntervaloERecusada() throws Exception {
        for (var quantidade : new int[] {0, 61}) {
            var resultado = aderir(",\"quantidadePeriodos\":" + quantidade);
            assertEquals(422, resultado.getResponse().getStatus());
            assertEquals("A recorrência deve ter de 1 a 60 cobranças",
                    json.readTree(resultado.getResponse().getContentAsString()).path("mensagem").asString());
        }
        assertEquals(0, banco.queryForObject("select count(*) from ADP_ADESAO_PLANO", Long.class));
    }

    @Test
    void alunoNaoDefineRecorrencia() throws Exception {
        var resultado = enviar(ADESOES + "aderir", aluno(),
                "{\"aluno\":\"%d\",\"plano\":\"%s\",\"quantidadePeriodos\":5}".formatted(mariana, mensal));
        assertEquals(403, resultado.getResponse().getStatus());
    }
}
