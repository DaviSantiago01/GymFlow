package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.equipamento;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import br.edu.cesar.treinoconecta.treinoconectacore.BackendAplicacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.Equipamento;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(classes = BackendAplicacao.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:equipamentos-integracao;DB_CLOSE_DELAY=-1",
        "treino.reservas.agendamento=false", "treino.grupos.agendamento=false"})
class EquipamentosIntegracaoTest {
    private static final String EQUIPAMENTOS = "/backend/core/equipamento/";
    private static final String REGISTROS = "/backend/core/registro-equipamento/";
    private static final String PROBLEMAS = "/backend/core/problema-equipamento/";
    private static final String ORDENS = "/backend/core/ordem-manutencao/";

    @Autowired
    private WebApplicationContext contexto;
    private final JsonMapper json = new JsonMapper();
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void consultaEAlteraSomenteComPapelAdministrativo() throws Exception {
        mvc.perform(get(EQUIPAMENTOS + "pesquisa").with(user("u").authorities(new SimpleGrantedAuthority("ALUNO"))))
                .andExpect(status().isForbidden());
        mvc.perform(post(EQUIPAMENTOS + "cadastrar").with(user("u").authorities(new SimpleGrantedAuthority("ALUNO")))
                .with(csrf()).contentType("application/json").content(json.writeValueAsString(dados("SEC-01"))))
                .andExpect(status().isForbidden());
        var id = identificador(enviar(EQUIPAMENTOS + "cadastrar", dados("SEC-01")));
        enviar(EQUIPAMENTOS + id + "/excluir", Map.of()).andExpect(status().is2xxSuccessful());
        consultar(EQUIPAMENTOS + "pesquisa?termo=SEC-01").andExpect(jsonPath("$.resultado.total").value(0));
    }

    @Test
    void persisteHistoricoEBloqueiaLiberacaoAteResolverEInspecionar() throws Exception {
        var hoje = LocalDate.now(Equipamento.FUSO);
        var equipamento = identificador(enviar(EQUIPAMENTOS + "cadastrar", dados("FLUXO-01")));
        var problema = identificador(enviar(PROBLEMAS + "registrar", Map.of(
                "equipamento", equipamento, "titulo", "Falha no motor", "relato", "Motor perde potência",
                "gravidade", "CRITICA", "risco", true, "dataRelato", hoje.toString())));
        consultar(EQUIPAMENTOS + equipamento + "/visualizacao")
                .andExpect(jsonPath("$.situacao").value("INTERDITADO"));
        consultar(EQUIPAMENTOS + equipamento + "/operacoes")
                .andExpect(jsonPath("$.podeExcluir").value(false));
        enviar(EQUIPAMENTOS + equipamento + "/excluir", Map.of()).andExpect(status().is4xxClientError());

        var liberar = Map.of("data", hoje.toString(), "resultado", "APROVADO",
                "area", "FUNCIONAL", "situacao", "DISPONIVEL");
        enviar(REGISTROS + equipamento + "/inspecionar", liberar).andExpect(status().is4xxClientError());
        var ordem = identificador(enviar(ORDENS + "abrir", Map.of(
                "problema", problema, "tecnico", "Técnico", "diagnostico", "Motor danificado")));
        enviar(ORDENS + ordem + "/iniciar", Map.of("data", hoje.toString())).andExpect(status().isOk());
        enviar(ORDENS + ordem + "/concluir", Map.of("data", hoje.toString())).andExpect(status().isBadRequest());
        enviar(ORDENS + ordem + "/concluir", Map.of(
                "data", hoje.toString(), "servicoExecutado", "Substituição do motor"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("CONCLUIDA"));
        consultar(PROBLEMAS + problema + "/visualizacao").andExpect(jsonPath("$.situacao").value("RESOLVIDO"));
        consultar(EQUIPAMENTOS + equipamento + "/visualizacao")
                .andExpect(jsonPath("$.situacao").value("EM_MANUTENCAO"));

        enviar(REGISTROS + equipamento + "/inspecionar", liberar).andExpect(status().isNoContent());
        consultar(EQUIPAMENTOS + equipamento + "/visualizacao")
                .andExpect(jsonPath("$.situacao").value("DISPONIVEL"))
                .andExpect(jsonPath("$.area").value("FUNCIONAL"))
                .andExpect(jsonPath("$.proximaInspecao").value(hoje.plusDays(30).toString()));
        enviar(EQUIPAMENTOS + equipamento + "/inativar", Map.of())
                .andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("INATIVO"));
        consultar(REGISTROS + "pesquisa?equipamento=" + equipamento)
                .andExpect(jsonPath("$.resultado.total").value(4));
    }

    private Map<String, Object> dados(String patrimonio) {
        return Map.of("patrimonio", patrimonio, "nome", "Esteira", "marca", "GymFlow", "modelo", "E1",
                "tipo", "CARDIO", "area", "CARDIO", "aquisicao", LocalDate.now(Equipamento.FUSO).toString(),
                "periodicidade", 30);
    }

    private static UserRequestPostProcessor administrador() {
        return user("admin").authorities(new SimpleGrantedAuthority("ADMINISTRADOR"));
    }

    private ResultActions enviar(String caminho, Map<String, ?> corpo) throws Exception {
        return mvc.perform(post(caminho).with(administrador()).with(csrf())
                .contentType("application/json").content(json.writeValueAsString(corpo)));
    }

    private ResultActions consultar(String caminho) throws Exception {
        return mvc.perform(get(caminho).with(administrador())).andExpect(status().isOk());
    }

    private String identificador(ResultActions resposta) throws Exception {
        var conteudo = resposta.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var identificador = json.readTree(conteudo).path("identificador");
        assertTrue(identificador.isString(), conteudo);
        return identificador.asString();
    }
}
