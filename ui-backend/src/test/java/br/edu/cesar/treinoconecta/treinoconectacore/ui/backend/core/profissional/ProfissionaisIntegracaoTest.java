package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.profissional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import br.edu.cesar.treinoconecta.treinoconectacore.BackendAplicacao;
import br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.LimpezaBanco;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(classes = BackendAplicacao.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:profissionais;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "treino.reservas.agendamento=false", "treino.grupos.agendamento=false"})
class ProfissionaisIntegracaoTest {
    private static final String PROFISSIONAIS = "/backend/core/profissional/";
    private static final String VINCULOS = "/backend/core/vinculo-personal-aluno/";
    private static final String RAFAEL = """
            {"nome":"Rafael Souza","cpf":"529.982.247-25","cref":"123456-g/pe","contato":"rafael@academia.com",
             "classificacoes":["PERSONAL_TRAINER"]}
            """;

    @Autowired
    private WebApplicationContext contexto;
    @Autowired
    private JdbcTemplate banco;
    private MockMvc mvc;
    private final JsonMapper json = new JsonMapper();
    private long mariana;

    @BeforeEach
    void preparar() {
        LimpezaBanco.limparDependentes(banco);
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        for (var tabela : new String[] {"PES_PROFISSIONAL_ESPECIALIDADE", "DSP_DISPONIBILIDADE_PROFISSIONAL", "PRF_PROFISSIONAL",
                "USU_CREDENCIAL", "ALU_ALUNO"}) {
            banco.update("delete from " + tabela);
        }
        banco.update("insert into ALU_ALUNO(ALU_USUARIO,ALU_NOME) values ('u','Mariana')");
        mariana = banco.queryForObject("select ALU_ID from ALU_ALUNO where ALU_USUARIO = 'u'", Long.class);
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

    private String cadastrarRafael() throws Exception {
        return sucesso(enviar(PROFISSIONAIS + "cadastrar", administrador(), RAFAEL)).path("profissional")
                .path("identificador").asString();
    }

    private JsonNode disponibilidade(String profissional, String dia, String inicio, String fim) throws Exception {
        return corpo(enviar(PROFISSIONAIS + profissional + "/disponibilidades/adicionar", administrador(),
                "{\"dia\":\"%s\",\"inicio\":\"%s\",\"fim\":\"%s\"}".formatted(dia, inicio, fim)));
    }

    @Test
    void cadastroNormalizaDocumentosEImpedeDuplicidade() throws Exception {
        var detalhe = sucesso(enviar(PROFISSIONAIS + "cadastrar", administrador(), RAFAEL));
        var profissional = detalhe.path("profissional");
        assertEquals("52998224725", profissional.path("cpf").asString());
        assertEquals("529.982.247-25", detalhe.path("cpfFormatado").asString());
        assertEquals("123456-G/PE", profissional.path("cref").asString());
        assertEquals("ATIVO", profissional.path("situacao").asString());
        assertEquals(2640, detalhe.path("limiteSemanalMinutos").asInt());

        falha(enviar(PROFISSIONAIS + "cadastrar", administrador(), RAFAEL.replace("Rafael Souza", "Outro")
                .replace("123456-g/pe", "654321-G/SP")), 409, "Já existe outro profissional cadastrado com este CPF");
        falha(enviar(PROFISSIONAIS + "cadastrar", administrador(), RAFAEL.replace("529.982.247-25", "390.533.447-05")), 409,
                "Já existe outro profissional cadastrado com este CREF");
        falha(enviar(PROFISSIONAIS + "cadastrar", administrador(), RAFAEL.replace("529.982.247-25", "123.456.789-00")), 422,
                "Informe um CPF válido");
        falha(enviar(PROFISSIONAIS + "cadastrar", administrador(), RAFAEL.replace("[\"PERSONAL_TRAINER\"]", "[]")
                .replace("529.982.247-25", "390.533.447-05").replace("123456-g/pe", "654321-G/SP")), 422,
                "Informe ao menos uma classificação: professor, personal trainer ou ambos");
    }

    @Test
    void especialidadesEDisponibilidadeRespeitamAsRegras() throws Exception {
        var rafael = cadastrarRafael();
        sucesso(enviar(PROFISSIONAIS + rafael + "/especialidades/adicionar", administrador(), "{\"nome\":\"Musculação\"}"));
        falha(enviar(PROFISSIONAIS + rafael + "/especialidades/adicionar", administrador(), "{\"nome\":\"musculacao\"}"), 409,
                "Esta especialidade já está associada ao profissional");

        var segunda = disponibilidade(rafael, "MONDAY", "08:00", "12:00");
        assertEquals("08:00", segunda.path("profissional").path("disponibilidades").get(0).path("inicio").asString().substring(0, 5));
        assertEquals(240, segunda.path("cargaHorariaSemanalMinutos").asInt());
        assertEquals("O período se sobrepõe a outra disponibilidade do profissional no mesmo dia",
                disponibilidade(rafael, "MONDAY", "11:00", "13:00").path("mensagem").asString());
        assertEquals("O horário inicial deve ser anterior ao horário final",
                disponibilidade(rafael, "TUESDAY", "13:00", "09:00").path("mensagem").asString());
        for (var dia : new String[] {"TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"}) {
            disponibilidade(rafael, dia, "06:00", "16:00");
        }
        assertEquals("A carga horária semanal ultrapassaria o limite de 44 horas",
                disponibilidade(rafael, "SATURDAY", "08:00", "09:00").path("mensagem").asString());

        var identificador = segunda.path("profissional").path("disponibilidades").get(0).path("identificador").asLong();
        var alterada = sucesso(enviar(PROFISSIONAIS + rafael + "/disponibilidades/" + identificador + "/alterar", administrador(),
                "{\"dia\":\"MONDAY\",\"inicio\":\"09:00\",\"fim\":\"11:00\"}"));
        assertEquals(42 * 60, alterada.path("cargaHorariaSemanalMinutos").asInt());
        var removida = sucesso(enviar(PROFISSIONAIS + rafael + "/disponibilidades/" + identificador + "/remover", administrador(), ""));
        assertEquals(40 * 60, removida.path("cargaHorariaSemanalMinutos").asInt());
    }

    @Test
    void vinculosControlamAInativacaoDoPersonal() throws Exception {
        var rafael = cadastrarRafael();
        var vinculo = sucesso(enviar(VINCULOS + "vincular", administrador(),
                "{\"profissional\":\"%s\",\"aluno\":\"%d\"}".formatted(rafael, mariana)));
        assertEquals("ATIVO", vinculo.path("situacao").asString());
        assertEquals("Mariana", vinculo.path("alunoNome").asString());
        falha(enviar(VINCULOS + "vincular", administrador(), "{\"profissional\":\"%s\",\"aluno\":\"%d\"}".formatted(rafael, mariana)),
                409, "Já existe um vínculo ativo entre este personal trainer e o aluno");
        falha(enviar(PROFISSIONAIS + rafael + "/inativar", administrador(), ""), 409,
                "Encerre os vínculos ativos com alunos antes de inativar o profissional");

        var detalhe = sucesso(mvc.perform(get(PROFISSIONAIS + rafael + "/visualizacao").with(administrador())).andReturn());
        assertEquals(1, detalhe.path("vinculos").size());
        assertEquals(1, detalhe.path("alunos").size());

        sucesso(enviar(VINCULOS + vinculo.path("identificador").asString() + "/encerrar", administrador(), ""));
        var inativo = sucesso(enviar(PROFISSIONAIS + rafael + "/inativar", administrador(), ""));
        assertEquals("INATIVO", inativo.path("profissional").path("situacao").asString());
        assertEquals("ENCERRADO", inativo.path("vinculos").get(0).path("situacao").asString());
        falha(enviar(VINCULOS + "vincular", administrador(), "{\"profissional\":\"%s\",\"aluno\":\"%d\"}".formatted(rafael, mariana)),
                409, "Profissionais inativos não podem receber novos vínculos");
        assertEquals("ATIVO", sucesso(enviar(PROFISSIONAIS + rafael + "/reativar", administrador(), ""))
                .path("profissional").path("situacao").asString());
    }

    @Test
    void professorSemClassificacaoDePersonalNaoRecebeAlunos() throws Exception {
        var professora = sucesso(enviar(PROFISSIONAIS + "cadastrar", administrador(), RAFAEL.replace("Rafael Souza", "Larissa")
                .replace("PERSONAL_TRAINER", "PROFESSOR"))).path("profissional").path("identificador").asString();
        falha(enviar(VINCULOS + "vincular", administrador(), "{\"profissional\":\"%s\",\"aluno\":\"%d\"}".formatted(professora, mariana)),
                409, "Somente profissionais classificados como personal trainer podem ser vinculados a alunos");
    }

    @Test
    void pesquisaEAcessoSaoExclusivosDoAdministrador() throws Exception {
        cadastrarRafael();
        var pesquisa = sucesso(mvc.perform(get(PROFISSIONAIS + "pesquisa?busca=529.982&classificacao=PERSONAL_TRAINER")
                .with(administrador())).andReturn());
        assertEquals(1, pesquisa.path("resultado").path("total").asInt());
        assertEquals(403, mvc.perform(get(PROFISSIONAIS + "pesquisa").with(personal())).andReturn().getResponse().getStatus());
        assertEquals(403, enviar(PROFISSIONAIS + "cadastrar", personal(), RAFAEL).getResponse().getStatus());
        var identificador = banco.queryForObject("select PRF_ID from PRF_PROFISSIONAL", Long.class);
        assertEquals(403, enviar(PROFISSIONAIS + identificador + "/excluir", administrador(), "").getResponse().getStatus());
        assertEquals(1, banco.queryForObject("select count(*) from PRF_PROFISSIONAL", Long.class));
    }
}
