package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.seguranca;

import java.util.*;
import java.util.concurrent.*;
import br.edu.cesar.treinoconecta.treinoconectacore.BackendAplicacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.ICredencialRepositorio;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes=BackendAplicacao.class,properties={
    "spring.datasource.url=jdbc:h2:mem:cadastro;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
    "treino.reservas.agendamento=false"})
class CadastroIntegracaoTest {
    @Autowired WebApplicationContext contexto;
    @Autowired JdbcTemplate banco;
    @Autowired PasswordEncoder codificador;
    @MockitoSpyBean ICredencialRepositorio credenciais;
    MockMvc mvc;
    final JsonMapper json = new JsonMapper();
    static final String SENHA = "SenhaDeTeste123!";

    @BeforeEach void preparar() {
        mvc=MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        banco.update("delete from RSV_RESERVA_ARMARIO");
        banco.update("delete from USU_CREDENCIAL");
        banco.update("delete from ALU_ALUNO");
        banco.update("delete from ARM_ARMARIO");
        banco.update("insert into ARM_ARMARIO values ('C-01','Vestiário C','Padrão')");
    }
    String corpo(String nome, String usuario, String senha, String confirmacao) throws Exception {
        return json.writeValueAsString(Map.of("nome",nome,"usuario",usuario,"senha",senha,"confirmacaoSenha",confirmacao));
    }
    int cadastrar(String usuario) throws Exception {
        return mvc.perform(post("/backend/cadastro").with(csrf()).contentType("application/json")
            .content(corpo("Aluno Novo",usuario,SENHA,SENHA))).andReturn().getResponse().getStatus();
    }
    // csrf() dos demais testes instala um repositório de teste no filtro compartilhado.
    // Recriar o contexto permite verificar o CookieCsrfTokenRepository de produção.
    @org.springframework.test.annotation.DirtiesContext(methodMode=org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test void visitanteObtemTokenMasNaoCadastraSemCsrf() throws Exception {
        mvc.perform(get("/backend/cadastro")).andExpect(status().isOk()).andExpect(cookie().exists("XSRF-TOKEN"));
        mvc.perform(post("/backend/cadastro").contentType("application/json")
            .content(corpo("Aluno","novo",SENHA,SENHA))).andExpect(status().isForbidden());
        assertEquals(0L,banco.queryForObject("select count(*) from ALU_ALUNO",Long.class));
    }
    @Test void cadastroPermiteLoginRealEReservaComSenhaProtegida() throws Exception {
        assertEquals(201,cadastrar("Novo.Aluno"));
        var hash=banco.queryForObject("select USU_SENHA from USU_CREDENCIAL where ALU_USUARIO='novo.aluno'",String.class);
        assertNotEquals(SENHA,hash);
        assertTrue(hash.startsWith("{bcrypt}"));
        assertTrue(codificador.matches(SENHA,hash));
        var login=mvc.perform(get("/backend/autenticacao").with(httpBasic("NOVO.ALUNO",SENHA)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.usuario").value("novo.aluno")).andReturn();
        var sessao=(MockHttpSession)login.getRequest().getSession(false);
        mvc.perform(post("/backend/core/armario/C-01/reservar").session(sessao).with(csrf()).contentType("application/json")
            .content(json.writeValueAsString(Map.of("data",java.time.LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")).plusDays(1).toString(),"hora",6,"duracaoHoras",1))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.dados.alunoNome").value("Aluno Novo"));
        mvc.perform(get("/backend/autenticacao").with(httpBasic("novo.aluno","incorreta"))).andExpect(status().isUnauthorized());
    }
    @Test void nomeDuplicadoNaoSubstituiConta() throws Exception {
        assertEquals(201,cadastrar("aluno"));
        assertEquals(409,cadastrar(" ALUNO "));
        assertEquals(1L,banco.queryForObject("select count(*) from ALU_ALUNO",Long.class));
        assertEquals(1L,banco.queryForObject("select count(*) from USU_CREDENCIAL",Long.class));
    }
    @Test void rejeitaDadosInvalidosSemPersistirAluno() throws Exception {
        for(var dados:List.of(
            List.of("A","aluno",SENHA,SENHA),
            List.of("Aluno","a",SENHA,SENHA),
            List.of("Aluno","aluno com espaço",SENHA,SENHA),
            List.of("Aluno","aluno","123","123"),
            List.of("Aluno","aluno",SENHA,"outraSenha"),
            List.of("Aluno","aluno","á".repeat(40),"á".repeat(40)))) {
            mvc.perform(post("/backend/cadastro").with(csrf()).contentType("application/json")
                .content(corpo(dados.get(0),dados.get(1),dados.get(2),dados.get(3)))).andExpect(status().isUnprocessableEntity());
        }
        assertEquals(0L,banco.queryForObject("select count(*) from ALU_ALUNO",Long.class));
    }
    @Test void concorrenciaConfirmaSomenteUmCadastro() throws Exception {
        var prontos=new CountDownLatch(2);var inicio=new CountDownLatch(1);
        try(var executor=Executors.newFixedThreadPool(2)) {
            Callable<Integer> tarefa=()->{prontos.countDown();inicio.await();return cadastrar("concorrente");};
            var a=executor.submit(tarefa);var b=executor.submit(tarefa);
            assertTrue(prontos.await(5,TimeUnit.SECONDS));inicio.countDown();
            var codigos=new ArrayList<>(List.of(a.get(20,TimeUnit.SECONDS),b.get(20,TimeUnit.SECONDS)));
            Collections.sort(codigos);assertEquals(List.of(201,409),codigos);
        }
        assertEquals(1L,banco.queryForObject("select count(*) from ALU_ALUNO",Long.class));
        assertEquals(1L,banco.queryForObject("select count(*) from USU_CREDENCIAL",Long.class));
    }
    @Test void falhaNaCredencialDesfazCriacaoDoAluno() throws Exception {
        doThrow(new IllegalStateException("Falha simulada na persistência da credencial")).when(credenciais).incluir(eq("falha"),anyString());
        assertEquals(409,cadastrar("falha"));
        assertEquals(0L,banco.queryForObject("select count(*) from ALU_ALUNO",Long.class));
        assertEquals(0L,banco.queryForObject("select count(*) from USU_CREDENCIAL",Long.class));
    }
}
