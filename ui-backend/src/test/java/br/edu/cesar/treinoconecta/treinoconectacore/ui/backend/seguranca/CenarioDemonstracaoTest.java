package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.seguranca;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import br.edu.cesar.treinoconecta.treinoconectacore.BackendAplicacao;

@ActiveProfiles("desenvolvimento")
@SpringBootTest(classes = BackendAplicacao.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:cenario-demonstracao;DB_CLOSE_DELAY=-1",
        "treino.reservas.agendamento=false", "treino.grupos.agendamento=false"})
class CenarioDemonstracaoTest {
    @Autowired WebApplicationContext contexto;
    @Autowired CenarioDemonstracao cenario;
    @Autowired JdbcTemplate banco;

    @Test
    void exemplosAparecemNasConsultasDosTresPerfis() throws Exception {
        var mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();

        consultar(mvc, "gestor", "plano/pesquisa", "Mensal");
        consultar(mvc, "gestor", "profissional/pesquisa", "Bruno Almeida");
        consultar(mvc, "gestor", "adesao-plano/pesquisa", "Mensal");
        consultar(mvc, "gestor", "mensalidade/pesquisa", "PAGA");

        consultar(mvc, "professor", "aluno/pesquisa", "Mariana Costa");
        consultar(mvc, "professor", "plano-de-treino/pesquisa", "Força e técnica");

        consultar(mvc, "aluno", "adesao-plano/meu-plano", "Mensal");
        consultar(mvc, "aluno", "grupo-treino/pesquisa", "Treino em equipe");
        consultar(mvc, "aluno", "armario/pesquisa", "A-02");
        consultar(mvc, "aluno", "reserva-armario/pesquisa", "ENCERRADA");
        consultar(mvc, "aluno", "avaliacao-professor/pesquisa", "Exemplo fictício");
        verificarTotal(mvc, "aluno", "execucao-de-exercicio/pesquisa", 3);
        verificarTotal(mvc, "aluno", "avaliacao-fisica/pesquisa", 3);
        verificarTotal(mvc, "aluno", "meta/pesquisa?ordenacao=FIM", 1);
    }

    @Test
    void recargaPreservaRegistrosEditadosESemDuplicatas() {
        banco.update("update GRT_GRUPO_TREINO set GRT_NOME = ? where GRT_NOME = ?",
                "Equipe editada", "Treino em equipe · demonstração");
        try {
            cenario.carregar();
            assertEquals(1, banco.queryForObject("select count(*) from GRT_GRUPO_TREINO", Integer.class));
            assertEquals("Equipe editada", banco.queryForObject("select GRT_NOME from GRT_GRUPO_TREINO", String.class));
            assertEquals(3, banco.queryForObject("select count(*) from MEN_MENSALIDADE", Integer.class));
        } finally {
            banco.update("update GRT_GRUPO_TREINO set GRT_NOME = ? where GRT_NOME = ?",
                    "Treino em equipe · demonstração", "Equipe editada");
        }
    }

    private void consultar(MockMvc mvc, String usuario, String caminho, String conteudo) throws Exception {
        mvc.perform(get("/backend/core/" + caminho).with(httpBasic(usuario, usuario)))
                .andExpect(status().isOk()).andExpect(content().string(containsString(conteudo)));
    }

    private void verificarTotal(MockMvc mvc, String usuario, String caminho, int total) throws Exception {
        mvc.perform(get("/backend/core/" + caminho).with(httpBasic(usuario, usuario)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.resultado.total").value(total));
    }
}
