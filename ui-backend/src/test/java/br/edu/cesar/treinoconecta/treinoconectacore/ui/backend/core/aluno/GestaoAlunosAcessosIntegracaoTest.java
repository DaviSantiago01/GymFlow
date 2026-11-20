package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.aluno;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import br.edu.cesar.treinoconecta.treinoconectacore.BackendAplicacao;
import br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.LimpezaBanco;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** F005 (cadastro e gerenciamento de alunos) e F006 (controle de entrada e frequência). */
@SpringBootTest(classes = BackendAplicacao.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:gestaoalunos;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "treino.reservas.agendamento=false", "treino.grupos.agendamento=false",
        "treino.acesso.capacidade-maxima=2", "treino.acesso.abertura=00:00", "treino.acesso.fechamento=00:00"})
class GestaoAlunosAcessosIntegracaoTest {
    private static final String ALUNOS = "/backend/core/aluno/";
    private static final String ACESSOS = "/backend/core/registro-acesso/";

    @Autowired
    private WebApplicationContext contexto;
    @Autowired
    private JdbcTemplate banco;
    private MockMvc mvc;
    private final JsonMapper json = new JsonMapper();
    private long plano;

    @BeforeEach
    void preparar() {
        LimpezaBanco.limparDependentes(banco);
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        for (var tabela : new String[] {"PLA_PLANO", "USU_CREDENCIAL", "ALU_ALUNO"}) {
            banco.update("delete from " + tabela);
        }
        banco.update("insert into PLA_PLANO(PLA_NOME,PLA_VALOR,PLA_DURACAO_MESES,PLA_SITUACAO) values ('Mensal',99.90,1,'ATIVO')");
        plano = banco.queryForObject("select PLA_ID from PLA_PLANO", Long.class);
    }

    /** Gera um CPF válido a partir de nove dígitos. */
    private static String cpf(String nove) {
        var digitos = new StringBuilder(nove);
        for (int quantidade = 9; quantidade <= 10; quantidade++) {
            int soma = 0;
            for (int i = 0; i < quantidade; i++) {
                soma += (digitos.charAt(i) - '0') * (quantidade + 1 - i);
            }
            int resto = soma % 11;
            digitos.append(resto < 2 ? 0 : 11 - resto);
        }
        return digitos.toString();
    }

    private static UserRequestPostProcessor administrador() {
        return user("admin").authorities(new SimpleGrantedAuthority("ADMINISTRADOR"));
    }

    private static UserRequestPostProcessor personal() {
        return user("p").authorities(new SimpleGrantedAuthority("PERSONAL"));
    }

    private MvcResult enviar(String caminho, UserRequestPostProcessor usuario, String corpo) throws Exception {
        return mvc.perform(post(caminho).with(usuario).with(csrf()).contentType("application/json").content(corpo)).andReturn();
    }

    private MvcResult consultar(String caminho, UserRequestPostProcessor usuario) throws Exception {
        return mvc.perform(get(caminho).with(usuario)).andReturn();
    }

    private JsonNode sucesso(MvcResult resultado) throws Exception {
        assertEquals(200, resultado.getResponse().getStatus(), resultado.getResponse().getContentAsString());
        return json.readTree(resultado.getResponse().getContentAsString());
    }

    private void falha(MvcResult resultado, int status, String mensagem) throws Exception {
        assertEquals(status, resultado.getResponse().getStatus(), resultado.getResponse().getContentAsString());
        assertEquals(mensagem, json.readTree(resultado.getResponse().getContentAsString()).path("mensagem").asString());
    }

    private static String aluno(String nome, String usuario, String cpf, String nascimento, String email,
            String responsavel) {
        return """
                {"nome":"%s","usuario":"%s","senha":"Senha123!","cpf":"%s","nascimento":"%s","email":"%s",
                 "telefone":"(81) 99999-0000","endereco":"Rua A, 10 - Recife/PE"%s}
                """.formatted(nome, usuario, cpf, nascimento, email, responsavel);
    }

    private String cadastrar(String nome, String usuario, String cpf, String email) throws Exception {
        return sucesso(enviar(ALUNOS + "gestao/cadastrar", administrador(),
                aluno(nome, usuario, cpf, "1995-03-10", email, ""))).path("identificador").asString();
    }

    private void aderir(String aluno) throws Exception {
        sucesso(enviar("/backend/core/adesao-plano/aderir", administrador(),
                "{\"aluno\":\"%s\",\"plano\":\"%d\"}".formatted(aluno, plano)));
    }

    @Test
    void cadastraAlunoComDadosCompletosEValidaRegras() throws Exception {
        var ficha = sucesso(enviar(ALUNOS + "gestao/cadastrar", administrador(),
                aluno("  Ana   Lima ", "Ana.Lima", cpf("111444777"), "1995-03-10", "ANA@Mail.com", "")));
        assertEquals("Ana Lima", ficha.path("nome").asString());
        assertEquals("ana.lima", ficha.path("usuario").asString());
        assertEquals("111.444.777-35", ficha.path("cpf").asString());
        assertEquals("ana@mail.com", ficha.path("email").asString());
        assertEquals("SEM_MATRICULA", ficha.path("situacao").asString());
        assertEquals("CADASTRO", ficha.path("historico").get(0).path("tipo").asString());
        assertEquals("admin", ficha.path("historico").get(0).path("responsavel").asString());
        assertEquals(1L, banco.queryForObject("select count(*) from USU_CREDENCIAL where ALU_USUARIO = 'ana.lima'", Long.class));

        falha(enviar(ALUNOS + "gestao/cadastrar", administrador(),
                aluno("Outra Ana", "outra", "111.444.777-35", "1990-01-01", "", "")), 409,
                "Já existe outro aluno cadastrado com este CPF");
        falha(enviar(ALUNOS + "gestao/cadastrar", administrador(),
                aluno("Outra Ana", "outra", cpf("123456789"), "1990-01-01", "ana@MAIL.com", "")), 409,
                "Já existe outro aluno cadastrado com este e-mail");
        falha(enviar(ALUNOS + "gestao/cadastrar", administrador(),
                aluno("Jo", "jo", cpf("123456789"), "1990-01-01", "", "")), 422,
                "Informe o nome do aluno com 3 a 100 caracteres");
        falha(enviar(ALUNOS + "gestao/cadastrar", administrador(),
                aluno("Bia Souza", "bia", "123.456.789-00", "1990-01-01", "", "")), 422, "Informe um CPF válido");
        falha(enviar(ALUNOS + "gestao/cadastrar", administrador(),
                aluno("Bia Souza", "bia", cpf("123456789"), LocalDate.now().plusDays(2).toString(), "", "")), 422,
                "A data de nascimento não pode ser posterior à data atual");
        var menor = LocalDate.now().minusYears(15).toString();
        falha(enviar(ALUNOS + "gestao/cadastrar", administrador(),
                aluno("Bia Souza", "bia", cpf("123456789"), menor, "", "")), 422,
                "Alunos menores de 18 anos devem ter um responsável legal");
        var comResponsavel = sucesso(enviar(ALUNOS + "gestao/cadastrar", administrador(),
                aluno("Bia Souza", "bia", cpf("123456789"), menor, "",
                        ",\"responsavelNome\":\"Carla Souza\",\"responsavelCpf\":\"" + cpf("987654321") + "\"")));
        assertTrue(comResponsavel.path("menorDeIdade").asBoolean());
        assertEquals("Carla Souza", comResponsavel.path("responsavel").path("nome").asString());

        assertEquals(403, enviar(ALUNOS + "gestao/cadastrar", personal(),
                aluno("Caio Reis", "caio", cpf("222333444"), "1990-01-01", "", "")).getResponse().getStatus());
        assertEquals(403, consultar(ALUNOS + "gestao", personal()).getResponse().getStatus());
    }

    @Test
    void alteraBloqueiaERegistraHistorico() throws Exception {
        var ana = cadastrar("Ana Lima", "ana", cpf("111444777"), "ana@mail.com");
        cadastrar("Bruno Dias", "bruno", cpf("123456789"), "bruno@mail.com");
        falha(enviar(ALUNOS + ana + "/editar", administrador(),
                aluno("Ana Lima", "", cpf("123456789"), "1995-03-10", "", "")), 409,
                "Já existe outro aluno cadastrado com este CPF");
        var editada = sucesso(enviar(ALUNOS + ana + "/editar", administrador(),
                aluno("Ana Lima Costa", "", cpf("111444777"), "1995-03-10", "nova@mail.com", "")));
        assertEquals("Ana Lima Costa", editada.path("nome").asString());
        assertEquals("Alterado: nome, e-mail", editada.path("historico").get(0).path("descricao").asString());

        var bloqueada = sucesso(enviar(ALUNOS + ana + "/bloquear", administrador(), "{\"motivo\":\"Pendência de exame\"}"));
        assertEquals("BLOQUEADO", bloqueada.path("situacao").asString());
        assertEquals("Pendência de exame", bloqueada.path("motivoBloqueio").asString());
        falha(enviar(ALUNOS + ana + "/bloquear", administrador(), "{\"motivo\":\"De novo\"}"), 409,
                "O aluno já possui um bloqueio ativo");
        var lista = sucesso(consultar(ALUNOS + "gestao?situacao=BLOQUEADO", administrador()));
        assertEquals(1, lista.size());
        assertEquals("Ana Lima Costa", lista.get(0).path("nome").asString());
        assertEquals(1, sucesso(consultar(ALUNOS + "gestao?busca=123.456", administrador())).size());

        var desbloqueada = sucesso(enviar(ALUNOS + ana + "/desbloquear", administrador(), ""));
        assertEquals("SEM_MATRICULA", desbloqueada.path("situacao").asString());
        assertEquals(4, desbloqueada.path("historico").size());
    }

    @Test
    void excluiSemVinculosEDesativaComMatricula() throws Exception {
        var ana = cadastrar("Ana Lima", "ana", cpf("111444777"), "");
        var bruno = cadastrar("Bruno Dias", "bruno", cpf("123456789"), "");
        assertEquals("EXCLUIDO", sucesso(enviar(ALUNOS + ana + "/remover", administrador(), "")).path("resultado").asString());
        assertEquals(0L, banco.queryForObject("select count(*) from ALU_ALUNO where ALU_USUARIO = 'ana'", Long.class));

        aderir(bruno);
        assertEquals("ATIVO", sucesso(consultar(ALUNOS + bruno + "/ficha", administrador())).path("situacao").asString());
        assertEquals("DESATIVADO",
                sucesso(enviar(ALUNOS + bruno + "/remover", administrador(), "")).path("resultado").asString());
        var ficha = sucesso(consultar(ALUNOS + bruno + "/ficha", administrador()));
        assertEquals("INATIVO", ficha.path("situacao").asString());
        assertEquals("Mensal", ficha.path("matricula").path("plano").asString());
        falha(enviar(ACESSOS + "aluno/" + bruno + "/entrada", administrador(), ""), 409,
                "Entrada negada: o cadastro do aluno está inativo");
        assertEquals("ATIVO", sucesso(enviar(ALUNOS + bruno + "/reativar", administrador(), "")).path("situacao").asString());
    }

    @Test
    void controlaEntradaSaidaCapacidadeEFrequencia() throws Exception {
        var ana = cadastrar("Ana Lima", "ana", cpf("111444777"), "");
        var bruno = cadastrar("Bruno Dias", "bruno", cpf("123456789"), "");
        var caio = cadastrar("Caio Reis", "caio", cpf("222333444"), "");

        falha(enviar(ACESSOS + "aluno/" + ana + "/entrada", administrador(), ""), 409,
                "Entrada negada: o aluno não possui matrícula ativa");
        aderir(ana);
        aderir(bruno);
        aderir(caio);

        var painel = sucesso(enviar(ACESSOS + "aluno/" + ana + "/entrada", administrador(), ""));
        assertEquals(1, painel.path("presentes").asInt());
        assertEquals(2, painel.path("capacidadeMaxima").asInt());
        falha(enviar(ACESSOS + "aluno/" + ana + "/entrada", administrador(), ""), 409,
                "O aluno já está na academia. Registre a saída antes de uma nova entrada");
        sucesso(enviar(ACESSOS + "aluno/" + bruno + "/entrada", administrador(), ""));
        falha(enviar(ACESSOS + "aluno/" + caio + "/entrada", administrador(), ""), 409,
                "Entrada negada: a academia atingiu a capacidade máxima de 2 alunos");
        assertTrue(sucesso(consultar(ALUNOS + ana + "/ficha", administrador())).path("presente").asBoolean());

        var saida = sucesso(enviar(ACESSOS + "aluno/" + ana + "/saida", administrador(), ""));
        assertEquals(1, saida.path("presentes").asInt());
        falha(enviar(ACESSOS + "aluno/" + ana + "/saida", administrador(), ""), 409, "Não há entrada aberta para este aluno");
        sucesso(enviar(ACESSOS + "aluno/" + caio + "/entrada", administrador(), ""));

        sucesso(enviar(ALUNOS + ana + "/bloquear", administrador(), "{\"motivo\":\"Cartão perdido\"}"));
        falha(enviar(ACESSOS + "aluno/" + ana + "/entrada", administrador(), ""), 409,
                "Entrada negada: o aluno está bloqueado (Cartão perdido)");
        sucesso(enviar(ALUNOS + ana + "/desbloquear", administrador(), ""));

        banco.update("update MEN_MENSALIDADE set MEN_VENCIMENTO = MEN_PERIODO_INICIO, MEN_PERIODO_INICIO = DATEADD(DAY, -40, MEN_PERIODO_INICIO), MEN_PERIODO_FIM = MEN_PERIODO_FIM where ALU_ID = ?",
                Long.valueOf(ana));
        banco.update("update MEN_MENSALIDADE set MEN_VENCIMENTO = DATEADD(DAY, -5, CURRENT_DATE) where ALU_ID = ?",
                Long.valueOf(ana));
        falha(enviar(ACESSOS + "aluno/" + ana + "/entrada", administrador(), ""), 409,
                "Entrada negada: o plano está fora da validade, com mensalidade em atraso");
        assertEquals("INADIMPLENTE", sucesso(consultar(ALUNOS + ana + "/ficha", administrador())).path("situacao").asString());

        // Duas visitas antigas em dias diferentes, uma delas acima do limite de permanência de 4 horas.
        banco.update("""
                insert into RAC_REGISTRO_ACESSO(ALU_ID,RAC_ENTRADA,RAC_SAIDA,RAC_VERIFICAR) values
                 (?, DATEADD(HOUR, -50, CURRENT_TIMESTAMP), DATEADD(HOUR, -49, CURRENT_TIMESTAMP), false),
                 (?, DATEADD(HOUR, -100, CURRENT_TIMESTAMP), DATEADD(HOUR, -94, CURRENT_TIMESTAMP), true)
                """, Long.valueOf(ana), Long.valueOf(ana));
        var frequencia = sucesso(consultar(ACESSOS + "aluno/" + ana + "/frequencia", administrador()));
        assertEquals(3, frequencia.path("visitas").asInt());
        assertTrue(frequencia.path("diasComparecidos").asInt() >= 2);
        assertEquals(30, frequencia.path("diasNoPeriodo").asInt());
        assertTrue(frequencia.path("registros").get(2).path("excedeuLimite").asBoolean());
        assertFalse(frequencia.path("registros").get(1).path("excedeuLimite").asBoolean());
        assertEquals(420, frequencia.path("minutosTotais").asInt(), 2);

        assertEquals(403, consultar(ACESSOS + "presentes", personal()).getResponse().getStatus());
        var historico = sucesso(consultar(ACESSOS + "pesquisa?aluno=" + ana + "&tamanhoPagina=50", administrador()));
        assertEquals(3, historico.path("resultado").path("projecoes").size());
    }
}
