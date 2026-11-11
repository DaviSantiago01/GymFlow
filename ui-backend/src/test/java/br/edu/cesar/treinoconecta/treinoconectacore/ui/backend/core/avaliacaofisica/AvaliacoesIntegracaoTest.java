package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.avaliacaofisica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDate;
import java.time.ZoneId;
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
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.MetaServico;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(classes = BackendAplicacao.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:avaliacoes;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "treino.reservas.agendamento=false"})
class AvaliacoesIntegracaoTest {
    private static final String AVALIACOES = "/backend/core/avaliacao-fisica/";
    private static final String METAS = "/backend/core/meta/";

    @Autowired
    private WebApplicationContext contexto;
    @Autowired
    private JdbcTemplate banco;
    private MockMvc mvc;
    private final JsonMapper json = new JsonMapper();
    private final LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
    private long alunoId;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        for (var tabela : new String[] {"MET_META", "MDC_MEDIDA_CORPORAL", "AVF_AVALIACAO_FISICA", "EXC_EXECUCAO_EXERCICIO",
                "SRE_SERIE", "TRE_TREINO", "PLT_PLANO_TREINO", "PCD_CREDENCIAL_PERSONAL", "PER_PERSONAL", "ALU_ALUNO"}) {
            banco.update("delete from " + tabela);
        }
        banco.update("insert into ALU_ALUNO(ALU_USUARIO,ALU_NOME) values ('u','Mariana'),('v','Pedro')");
        banco.update("insert into PER_PERSONAL(PER_USUARIO,PER_NOME) values ('p','Rafael')");
        alunoId = banco.queryForObject("select ALU_ID from ALU_ALUNO where ALU_USUARIO = 'u'", Long.class);
    }

    private static UserRequestPostProcessor personal() {
        return user("p").authorities(new SimpleGrantedAuthority("PERSONAL"));
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

    private static String dadosDaAvaliacao(LocalDate data, String cintura) {
        return "\"data\":\"%s\",\"peso\":80,\"altura\":180,\"medidas\":[{\"indicador\":\"CINTURA\",\"valor\":%s}]".formatted(data, cintura);
    }

    private String registrar(LocalDate data, String cintura) throws Exception {
        var resposta = enviar(AVALIACOES + "registrar", personal(), "{\"aluno\":\"%d\",%s}".formatted(alunoId, dadosDaAvaliacao(data, cintura)));
        return sucesso(resposta).path("avaliacao").path("identificador").asString();
    }

    private MvcResult criarMeta(String desejado, LocalDate inicio, LocalDate fim) throws Exception {
        return enviar(METAS + "criar", personal(), "{\"aluno\":\"%d\",\"indicador\":\"CINTURA\",\"valorDesejado\":%s,\"inicio\":\"%s\",\"fim\":\"%s\"}"
                .formatted(alunoId, desejado, inicio, fim));
    }

    private String mensagem(MvcResult resultado) throws Exception {
        return corpo(resultado).path("mensagem").asString();
    }

    @Test
    void formularioTrazAlunosEMedidas() throws Exception {
        var formulario = sucesso(consultar(AVALIACOES + "inclusao", personal()));
        assertEquals(2, formulario.path("alunos").size());
        assertEquals("CINTURA", formulario.path("medidas").get(0).asString());
        assertEquals(20, formulario.path("pesoMinimo").asInt());
    }

    @Test
    void registraResultadoComImcClassificado() throws Exception {
        var id = registrar(hoje.minusDays(5), "90");
        var detalhe = sucesso(consultar(AVALIACOES + id + "/visualizacao", personal()));
        assertEquals(24.69, detalhe.path("avaliacao").path("imc").asDouble());
        assertEquals("PESO_NORMAL", detalhe.path("avaliacao").path("classificacaoImc").asString());
        assertEquals("CINTURA", detalhe.path("medidas").get(0).path("indicador").asString());
        assertTrue(detalhe.path("gerenciavel").asBoolean());
        assertFalse(sucesso(consultar(AVALIACOES + id + "/visualizacao", aluno("u"))).path("gerenciavel").asBoolean());
    }

    @Test
    void dataFuturaEMedidaInvalidaInformamOMotivo() throws Exception {
        var futura = enviar(AVALIACOES + "registrar", personal(), "{\"aluno\":\"%d\",%s}".formatted(alunoId, dadosDaAvaliacao(hoje.plusDays(1), "90")));
        assertEquals(422, futura.getResponse().getStatus());
        assertEquals(AvaliacaoFisicaServico.DATA_FUTURA, mensagem(futura));
        var medida = enviar(AVALIACOES + "registrar", personal(), "{\"aluno\":\"%d\",%s}".formatted(alunoId, dadosDaAvaliacao(hoje, "301")));
        assertEquals(422, medida.getResponse().getStatus());
        assertEquals("A medida CINTURA deve estar entre 1 e 300 cm", mensagem(medida));
    }

    @Test
    void outroAlunoNaoConsultaEAlunoNaoRegistra() throws Exception {
        var id = registrar(hoje.minusDays(5), "90");
        assertEquals(403, consultar(AVALIACOES + id + "/visualizacao", aluno("v")).getResponse().getStatus());
        assertEquals(403, consultar(AVALIACOES + "situacao?aluno=" + alunoId, aluno("v")).getResponse().getStatus());
        assertEquals(403, enviar(AVALIACOES + "registrar", aluno("u"), "{\"aluno\":\"%d\",%s}".formatted(alunoId, dadosDaAvaliacao(hoje, "90"))).getResponse().getStatus());
    }

    @Test
    void comparaComAReferenciaAnterior() throws Exception {
        registrar(hoje.minusDays(20), "95");
        var anterior = registrar(hoje.minusDays(10), "90");
        var atual = registrar(hoje.minusDays(1), "85");

        var comparacao = sucesso(consultar(AVALIACOES + atual + "/comparacao", personal()));

        assertEquals(anterior, comparacao.path("referencia").path("identificador").asString());
        assertEquals(2, comparacao.path("disponiveis").size());
        for (var indicador : comparacao.path("indicadores")) {
            if (indicador.path("indicador").asString().equals("CINTURA")) {
                assertEquals(5.0, indicador.path("diferencaAbsoluta").asDouble());
                assertEquals(5.56, indicador.path("diferencaPercentual").asDouble());
                assertEquals(-5.56, indicador.path("evolucaoPercentual").asDouble());
                assertEquals("MELHORA", indicador.path("interpretacao").asString());
            }
        }
    }

    @Test
    void evolucaoESituacaoUsamAsAvaliacoesValidas() throws Exception {
        registrar(hoje.minusDays(20), "95");
        registrar(hoje.minusDays(10), "90");
        registrar(hoje.minusDays(1), "85");

        var evolucao = sucesso(consultar(AVALIACOES + "evolucao?indicador=CINTURA", aluno("u")));
        assertEquals(3, evolucao.path("pontos").size());
        assertEquals("DIMINUINDO", evolucao.path("tendencia").asString());
        assertEquals("MELHORA", evolucao.path("interpretacao").asString());

        var situacao = sucesso(consultar(AVALIACOES + "situacao?aluno=" + alunoId, personal()));
        assertEquals(hoje.minusDays(1).toString(), situacao.path("avaliacao").path("data").asString());
        assertTrue(situacao.path("indicadores").size() >= 3);
    }

    @Test
    void correcaoPreservaAVersaoAnterior() throws Exception {
        var data = hoje.minusDays(3);
        var original = registrar(data, "90");

        var corrigida = sucesso(enviar(AVALIACOES + original + "/corrigir", personal(), "{%s}".formatted(dadosDaAvaliacao(data, "88"))));

        var novoId = corrigida.path("avaliacao").path("identificador").asString();
        assertFalse(original.equals(novoId));
        assertEquals(2, corrigida.path("avaliacao").path("versao").asInt());
        assertEquals(original, corrigida.path("versaoAnterior").asString());
        assertEquals("SUBSTITUIDA", sucesso(consultar(AVALIACOES + original + "/visualizacao", personal())).path("avaliacao").path("situacao").asString());
        var validas = sucesso(consultar(AVALIACOES + "pesquisa?situacao=VALIDA&aluno=" + alunoId, personal()));
        assertEquals(1, validas.path("resultado").path("total").asInt());
    }

    @Test
    void exclusaoRespeitaAsDependencias() throws Exception {
        var primeira = registrar(hoje.minusDays(10), "90");
        var ultima = registrar(hoje.minusDays(5), "88");

        var referencia = enviar(AVALIACOES + primeira + "/remover", personal(), "");
        assertEquals(409, referencia.getResponse().getStatus());
        assertEquals(AvaliacaoFisicaServico.REFERENCIA_DE_COMPARACAO, mensagem(referencia));

        assertEquals(200, criarMeta("80", hoje, hoje.plusDays(30)).getResponse().getStatus());
        var meta = enviar(AVALIACOES + ultima + "/remover", personal(), "");
        assertEquals(409, meta.getResponse().getStatus());
        assertEquals(AvaliacaoFisicaServico.REFERENCIA_DE_META, mensagem(meta));
    }

    @Test
    void ultimaAvaliacaoSemMetaEExcluidaLogicamente() throws Exception {
        registrar(hoje.minusDays(10), "90");
        var ultima = registrar(hoje.minusDays(5), "88");

        assertEquals(200, enviar(AVALIACOES + ultima + "/remover", personal(), "").getResponse().getStatus());

        assertEquals("EXCLUIDA", sucesso(consultar(AVALIACOES + ultima + "/visualizacao", personal())).path("avaliacao").path("situacao").asString());
        var situacao = sucesso(consultar(AVALIACOES + "situacao?aluno=" + alunoId, personal()));
        assertEquals(hoje.minusDays(10).toString(), situacao.path("avaliacao").path("data").asString());
    }

    @Test
    void metaAtivaUnicaECumprimento() throws Exception {
        registrar(hoje.minusDays(10), "90");
        var criada = sucesso(criarMeta("80", hoje.minusDays(5), hoje.plusDays(30)));
        var metaId = criada.path("identificador").asString();
        assertEquals("DISTANTE", criada.path("statusCumprimento").asString());

        var duplicada = criarMeta("75", hoje, hoje.plusDays(10));
        assertEquals(409, duplicada.getResponse().getStatus());
        assertEquals(MetaServico.META_ATIVA_EXISTENTE, mensagem(duplicada));

        registrar(hoje.minusDays(1), "82");
        var metas = sucesso(consultar(METAS + "pesquisa?aluno=" + alunoId, personal()));
        var projecao = metas.path("resultado").path("projecoes").get(0);
        assertEquals(metaId, projecao.path("identificador").asString());
        assertEquals(80.0, projecao.path("percentualCumprimento").asDouble());
        assertEquals("PROXIMA", projecao.path("statusCumprimento").asString());
    }

    @Test
    void novaAvaliacaoMarcaMetaAtingida() throws Exception {
        registrar(hoje.minusDays(10), "90");
        var metaId = sucesso(criarMeta("85", hoje.minusDays(5), hoje.plusDays(30))).path("identificador").asString();

        registrar(hoje.minusDays(1), "84");

        var recurso = sucesso(consultar(METAS + metaId + "/alteracao", personal()));
        assertTrue(recurso.path("formulario").path("indicadores").size() > 0);
        var meta = recurso.path("agregado");
        assertEquals("ATINGIDA", meta.path("situacao").asString());
        assertEquals(hoje.minusDays(1).toString(), meta.path("dataAtingida").asString());
    }

    @Test
    void alteracaoDaAvaliacaoRespondeOFormularioNaMesmaOperacao() throws Exception {
        var avaliacaoId = registrar(hoje.minusDays(3), "90");
        var recurso = sucesso(consultar(AVALIACOES + avaliacaoId + "/alteracao", personal()));
        assertEquals(avaliacaoId, recurso.path("agregado").path("identificador").asString());
        assertTrue(recurso.path("formulario").path("medidas").size() > 0);
    }

    @Test
    void ajusteSubstituicaoEEncerramentoDaMeta() throws Exception {
        registrar(hoje.minusDays(10), "90");
        var metaId = sucesso(criarMeta("80", hoje.minusDays(5), hoje.plusDays(30))).path("identificador").asString();

        var ajustada = sucesso(enviar(METAS + metaId + "/ajustar", personal(),
                "{\"valorDesejado\":78,\"inicio\":\"%s\",\"fim\":\"%s\"}".formatted(hoje.minusDays(5), hoje.plusDays(40))));
        assertEquals(78.0, ajustada.path("valorDesejado").asDouble());

        var nova = sucesso(enviar(METAS + metaId + "/substituir", personal(),
                "{\"valorDesejado\":75,\"inicio\":\"%s\",\"fim\":\"%s\"}".formatted(hoje, hoje.plusDays(40))));
        var novaId = nova.path("identificador").asString();
        assertFalse(metaId.equals(novaId));
        assertEquals("SUBSTITUIDA", sucesso(consultar(METAS + metaId + "/alteracao", personal())).path("agregado").path("situacao").asString());

        assertEquals("ENCERRADA", sucesso(enviar(METAS + novaId + "/encerrar", personal(), "")).path("situacao").asString());
        assertEquals(403, consultar(METAS + novaId + "/alteracao", aluno("v")).getResponse().getStatus());
    }

    @Test
    void correcaoMantemOVinculoDaMeta() throws Exception {
        var data = hoje.minusDays(10);
        var original = registrar(data, "90");
        assertEquals(200, criarMeta("80", hoje, hoje.plusDays(30)).getResponse().getStatus());

        var corrigida = sucesso(enviar(AVALIACOES + original + "/corrigir", personal(), "{%s}".formatted(dadosDaAvaliacao(data, "91"))));

        var novoId = corrigida.path("avaliacao").path("identificador").asString();
        assertEquals(1, banco.queryForObject("select count(*) from MET_META where AVF_ID = ?", Long.class, Long.valueOf(novoId)));
    }
}
