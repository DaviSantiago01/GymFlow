package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.avaliacaoprofessor;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import br.edu.cesar.treinoconecta.treinoconectacore.BackendAplicacao;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes={BackendAplicacao.class,AvaliacoesIntegracaoTest.Configuracao.class},properties={
    "spring.datasource.url=jdbc:h2:mem:avaliacoes;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000", "treino.reservas.agendamento=false"})
class AvaliacoesIntegracaoTest {
    @Autowired WebApplicationContext contexto;
    @Autowired JdbcTemplate banco;
    @Autowired RelogioTeste relogio;
    MockMvc mvc;
    final JsonMapper json=new JsonMapper();
    static final Instant INICIO=Instant.parse("2026-11-05T12:00:00Z");
    static final String TRN="/backend/core/treino/";
    static final String PRO="/backend/core/professor/",AVP="/backend/core/avaliacao-professor/";
    static final List<String> CRITERIOS=List.of("auxilio","pontualidade","postura","didatica","adequacao");
    String professor,segundo;
    @BeforeEach void preparar() {
        mvc=MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        for(String tabela:List.of("AVP_AVALIACAO_PROFESSOR","VAT_TREINO_AVALIADO","TRN_TREINO","VAP_AVALIACAO_REALIZADA","RSV_RESERVA_ARMARIO","USU_CREDENCIAL","ALU_ALUNO","PRO_PROFESSOR")) banco.update("delete from "+tabela);
        for(String nome:List.of("u","v")) inserirAluno(nome);
        banco.update("insert into PRO_PROFESSOR(PRO_NOME,PRO_ESPECIALIDADE,PRO_BIOGRAFIA) values ('Ana Souza','Musculação','Professora da academia'),('Bruno Lima','Mobilidade','Professor da academia')");
        professor=banco.queryForObject("select PRO_ID from PRO_PROFESSOR where PRO_NOME='Ana Souza'",Long.class).toString();
        segundo=banco.queryForObject("select PRO_ID from PRO_PROFESSOR where PRO_NOME='Bruno Lima'",Long.class).toString();
        relogio.agora.set(INICIO);
    }
    void inserirAluno(String nome) { banco.update("insert into ALU_ALUNO(ALU_USUARIO,ALU_NOME) values (?,?)",nome,"Aluno "+nome); }
    org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor aluno(String nome) { return user(nome).authorities(new SimpleGrantedAuthority("USUARIO")); }
    Map<String,Object> notas(int nota) { var m=new LinkedHashMap<String,Object>();CRITERIOS.forEach(c->m.put(c,nota));return m; }
    MvcResult comando(String rota,String usuario,Map<String,Object> corpo) throws Exception {
        return mvc.perform(post(rota).with(aluno(usuario)).with(csrf()).contentType("application/json").content(json.writeValueAsString(corpo))).andReturn();
    }
    String novoTreino(String usuario,String professor,boolean concluido,Instant inicio,Instant termino) {
        banco.update("insert into TRN_TREINO(ALU_ID,PRO_ID,TRN_DESCRICAO,TRN_INICIO,TRN_TERMINO,TRN_CONCLUIDO) select ALU_ID,?,'Treino real cadastrado',?,?,? from ALU_ALUNO where ALU_USUARIO=?",Long.valueOf(professor),inicio,termino,concluido,usuario);
        return banco.queryForObject("select max(TRN_ID) from TRN_TREINO",Long.class).toString();
    }
    String treino(String usuario,String professor) {
        var ids=banco.queryForList("select t.TRN_ID from TRN_TREINO t join ALU_ALUNO a on a.ALU_ID=t.ALU_ID where a.ALU_USUARIO=? and t.PRO_ID=? order by t.TRN_ID",Long.class,usuario,Long.valueOf(professor));
        if(!ids.isEmpty()) return ids.getFirst().toString();
        return novoTreino(usuario,professor,true,INICIO.minusSeconds(7200),INICIO.minusSeconds(3600));
    }
    MvcResult criar(String usuario,Map<String,Object> corpo) throws Exception { return comando(TRN+treino(usuario.equals("desconhecido")?"u":usuario,professor)+"/avaliar",usuario,corpo); }
    JsonNode dados(MvcResult resultado,int codigo) throws Exception { assertEquals(codigo,resultado.getResponse().getStatus(),resultado.getResponse().getContentAsString());return json.readTree(resultado.getResponse().getContentAsString()); }
    String id(MvcResult resultado) throws Exception { return dados(resultado,200).path("identificador").asText(); }
    JsonNode estatistica() throws Exception { return dados(mvc.perform(get(PRO+professor+"/estatistica").with(aluno("u"))).andReturn(),200); }
    long total(String tabela) { return banco.queryForObject("select count(*) from "+tabela,Long.class); }
    @Test void criaCincoNotasInteirasIncluindoZeroEPersiste() throws Exception {
        var n=notas(5);n.put("auxilio",0);n.put("comentario","Bom acompanhamento");var a=dados(criar("u",n),200);
        assertEquals(0,a.path("notas").path("auxilio").asInt());assertEquals("Bom acompanhamento",a.path("comentario").asText());assertTrue(a.path("propria").asBoolean());
        assertEquals(INICIO.toString(),a.path("criacao").asText());assertEquals(a.path("criacao"),a.path("atualizacao"));
        assertEquals(1,total("AVP_AVALIACAO_PROFESSOR"));assertEquals(1,total("VAT_TREINO_AVALIADO"));
    }
    static Stream<Arguments> notasInvalidas() { return CRITERIOS.stream().flatMap(c->Stream.of(-1,6,2.5).map(n->Arguments.of(c,n))); }
    @ParameterizedTest @MethodSource("notasInvalidas") void rejeitaForaDoIntervaloEDecimalEmTodosCriterios(String criterio,Number nota) throws Exception {
        var n=notas(3);n.put(criterio,nota);var erro=dados(criar("u",n),400);assertTrue(erro.path("campos").has(criterio));assertEquals(0,total("AVP_AVALIACAO_PROFESSOR"));assertEquals(0,total("VAT_TREINO_AVALIADO"));
    }
    @ParameterizedTest @ValueSource(strings={"auxilio","pontualidade","postura","didatica","adequacao"}) void rejeitaCriterioAusente(String criterio) throws Exception {
        var n=notas(4);n.remove(criterio);assertTrue(dados(criar("u",n),400).path("campos").has(criterio));
    }
    @Test void rejeitaNotaNula() throws Exception { var n=notas(3);n.put("didatica",null);dados(criar("u",n),400); }
    @Test void aceitaComentarioAusente() throws Exception { assertTrue(dados(criar("u",notas(4)),200).path("comentario").isNull()); }
    @ParameterizedTest @ValueSource(strings={"","   "}) void aceitaComentarioVazio(String comentario) throws Exception { var n=notas(3);n.put("comentario",comentario);assertTrue(dados(criar("u",n),200).path("comentario").isNull()); }
    @Test void aceitaComentarioNulo() throws Exception { var n=notas(3);n.put("comentario",null);assertTrue(dados(criar("u",n),200).path("comentario").isNull()); }
    @Test void comentarioTemLimiteExatoDe500() throws Exception {
        var n=notas(3);n.put("comentario","a".repeat(501));assertTrue(dados(criar("u",n),400).path("campos").has("comentario"));n.put("comentario","a".repeat(500));id(criar("u",n));
    }
    @Test void duplicadaERejeitadaMasOutroProfessorEPermitido() throws Exception {
        id(criar("u",notas(4)));dados(criar("u",notas(2)),409);id(comando(TRN+treino("u",segundo)+"/avaliar","u",notas(5)));assertEquals(2,total("AVP_AVALIACAO_PROFESSOR"));
    }
    @Test void concorrenciaDoMesmoParGravaSomenteUma() throws Exception {
        treino("u",professor);var largada=new CountDownLatch(1);
        try(var executor=Executors.newFixedThreadPool(2)) {
            var a=executor.submit(()->{largada.await();return criar("u",notas(3)).getResponse().getStatus();});
            var b=executor.submit(()->{largada.await();return criar("u",notas(5)).getResponse().getStatus();});largada.countDown();
            var codigos=new ArrayList<>(List.of(a.get(20,TimeUnit.SECONDS),b.get(20,TimeUnit.SECONDS)));Collections.sort(codigos);assertEquals(List.of(200,409),codigos);
        }assertEquals(1,total("AVP_AVALIACAO_PROFESSOR"));assertEquals(1,total("VAT_TREINO_AVALIADO"));
    }
    @Test void autorEditaNotasERemoveComentarioPreservandoCriacao() throws Exception {
        var n=notas(2);n.put("comentario","Antes");var id=id(criar("u",n));relogio.agora.set(INICIO.plusSeconds(60));
        var a=dados(comando(AVP+id+"/editar","u",notas(5)),200);assertEquals(5,a.path("notas").path("auxilio").asInt());assertTrue(a.path("comentario").isNull());
        assertEquals(INICIO.toString(),a.path("criacao").asText());assertEquals(INICIO.plusSeconds(60).toString(),a.path("atualizacao").asText());
    }
    @Test void outroAlunoNaoEditaNemExcluiEConsultaPrivadaNaoVaza() throws Exception {
        var id=id(criar("u",notas(2)));dados(comando(AVP+id+"/editar","v",notas(5)),403);
        mvc.perform(post(AVP+id+"/remover").with(aluno("v")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(get(AVP+id+"/visualizacao").with(aluno("v"))).andExpect(status().isNotFound());
        mvc.perform(get(AVP+"pesquisa").with(aluno("v"))).andExpect(jsonPath("$.resultado.total").value(0));assertEquals(2,estatistica().path("geral").asDouble());
    }
    @Test void autorExcluiFisicamenteEPreservaSomenteVinculoQueBloqueiaRecriacao() throws Exception {
        var id=id(criar("u",notas(5)));mvc.perform(post(AVP+id+"/remover").with(aluno("u")).with(csrf())).andExpect(status().isNoContent());
        assertEquals(0,total("AVP_AVALIACAO_PROFESSOR"));assertEquals(1,total("VAT_TREINO_AVALIADO"));dados(criar("u",notas(4)),409);
        mvc.perform(get(TRN+treino("u",professor)+"/minha-avaliacao").with(aluno("u"))).andExpect(jsonPath("$.jaAvaliou").value(true)).andExpect(jsonPath("$.avaliacao").isEmpty());
        mvc.perform(get(AVP+id+"/visualizacao").with(aluno("u"))).andExpect(status().isNotFound());
        assertTrue(estatistica().path("geral").isNull());
    }
    @Test void calculaMediaGeralPorCincoCriteriosSemArredondamentoPrematuro() throws Exception {
        var n=notas(0);for(int i=0;i<5;i++) n.put(CRITERIOS.get(i),i);id(criar("u",n));id(criar("v",notas(5)));
        var e=estatistica();assertEquals(2,e.path("quantidade").asLong());assertEquals(3.5,e.path("geral").asDouble());
        for(int i=0;i<5;i++) assertEquals((i+5)/2.0,e.path(CRITERIOS.get(i)).asDouble());
    }
    @Test void recalculaAposEdicao() throws Exception {
        var id=id(criar("u",notas(0)));id(criar("v",notas(5)));dados(comando(AVP+id+"/editar","u",notas(1)),200);assertEquals(3,estatistica().path("geral").asDouble());
    }
    @Test void recalculaAposExclusao() throws Exception {
        var id=id(criar("u",notas(0)));id(criar("v",notas(5)));mvc.perform(post(AVP+id+"/remover").with(aluno("u")).with(csrf())).andExpect(status().isNoContent());
        assertEquals(5,estatistica().path("geral").asDouble());assertEquals(1,estatistica().path("quantidade").asInt());
    }
    @Test void professorSemAvaliacoesTemMediasNulasEListaVazia() throws Exception {
        var e=estatistica();assertEquals(0,e.path("quantidade").asInt());for(String c:CRITERIOS) assertTrue(e.path(c).isNull());assertTrue(e.path("geral").isNull());
        mvc.perform(get(PRO+professor+"/visualizacao").with(aluno("u"))).andExpect(jsonPath("$.recentes").isEmpty());
    }
    @Test void zeroEUmaNotaRealEDiferenteDeSemAvaliacoes() throws Exception { id(criar("u",notas(0)));assertEquals(0,estatistica().path("geral").asDouble());assertFalse(estatistica().path("geral").isNull()); }
    @Test void dezRecentesOrdenadasPorCriacaoEEdicaoNaoMudaOrdem() throws Exception {
        List<String> ids=new ArrayList<>();
        for(int i=0;i<12;i++){String u="recente"+i;inserirAluno(u);relogio.agora.set(INICIO.plusSeconds(i));ids.add(id(criar(u,notas(i%6))));}
        relogio.agora.set(INICIO.plusSeconds(90));dados(comando(AVP+ids.getFirst()+"/editar","recente0",notas(5)),200);
        var recentes=dados(mvc.perform(get(PRO+professor+"/recentes").with(aluno("u"))).andReturn(),200);assertEquals(10,recentes.size());
        for(int i=0;i<10;i++) assertEquals(ids.get(11-i),recentes.get(i).path("identificador").asText());
        assertEquals(12,estatistica().path("quantidade").asInt());
    }
    @Test void datasIguaisSaoDesempatadasPorIdentificador() throws Exception {
        id(criar("u",notas(2)));var ultimo=id(criar("v",notas(4)));
        mvc.perform(get(PRO+professor+"/recentes").with(aluno("u"))).andExpect(jsonPath("$[0].identificador").value(ultimo));
    }
    @Test void buscaPaginadaEConsultaPropria() throws Exception {
        var id=id(criar("u",notas(5)));
        mvc.perform(get(PRO+"pesquisa").param("busca","ANA").with(aluno("u"))).andExpect(status().isOk()).andExpect(jsonPath("$.resultado.total").value(1)).andExpect(jsonPath("$.resultado.projecoes[0].estatisticas.geral").value(5));
        mvc.perform(get(PRO+"pesquisa").param("tamanhoPagina","1").param("indicePagina","1").with(aluno("u"))).andExpect(status().isOk()).andExpect(jsonPath("$.resultado.total").value(2)).andExpect(jsonPath("$.resultado.projecoes.length()").value(1));
        mvc.perform(get(TRN+treino("u",professor)+"/minha-avaliacao").with(aluno("u"))).andExpect(jsonPath("$.avaliacao.identificador").value(id));
        mvc.perform(get(AVP+"pesquisa").with(aluno("u"))).andExpect(jsonPath("$.resultado.total").value(1));
    }
    @Test void autorEnviadoNoCorpoNaoForjaIdentidade() throws Exception {
        var n=notas(4);n.put("aluno","v");n.put("professor",segundo);n.put("criacao","2099-01-01T00:00:00Z");var id=id(criar("u",n));
        assertEquals("u",banco.queryForObject("select a.ALU_USUARIO from AVP_AVALIACAO_PROFESSOR v join ALU_ALUNO a on a.ALU_ID=v.ALU_ID where AVP_ID=?",String.class,Long.valueOf(id)));
        mvc.perform(get(AVP+id+"/visualizacao").with(aluno("u"))).andExpect(jsonPath("$.professor").value(professor)).andExpect(jsonPath("$.criacao").value(INICIO.toString()));
    }
    @Test void exigeLoginCsrfEPapelDeAluno() throws Exception {
        mvc.perform(get(PRO+"pesquisa")).andExpect(status().isUnauthorized());
        mvc.perform(post(PRO+professor+"/avaliar").with(aluno("u"))).andExpect(status().isForbidden());
        dados(criar("desconhecido",notas(2)),403);
        mvc.perform(get(PRO+"pesquisa").with(user("u").roles("OUTRO"))).andExpect(status().isForbidden());
        assertEquals(0,total("AVP_AVALIACAO_PROFESSOR"));
    }
    @Test void operacoesCrudGenericasNaoContornamRegras() throws Exception {
        var id=id(criar("u",notas(3)));
        for(String acao:List.of("alteracao","exclusao")) {
            mvc.perform(get(AVP+id+"/"+acao).with(aluno("v"))).andExpect(status().isForbidden());
            mvc.perform(get(AVP+id+"/"+acao).with(aluno("u"))).andExpect(status().isOk()).andExpect(jsonPath("$.autorizacoes.alterar").value(false)).andExpect(jsonPath("$.autorizacoes.excluir").value(false));
        }
        var recurso=dados(mvc.perform(get(AVP+id+"/alteracao").with(aluno("u"))).andReturn(),200).path("agregado").toString();
        for(String rota:List.of(AVP+"incluir",AVP+id+"/alterar")) mvc.perform(post(rota).with(aluno("u")).with(csrf()).contentType("application/json").content(recurso)).andExpect(status().isForbidden());
        mvc.perform(post(AVP+id+"/excluir").with(aluno("u")).with(csrf())).andExpect(status().isForbidden());
        assertEquals(1,total("AVP_AVALIACAO_PROFESSOR"));
    }
    @Test void professorInexistenteNaoDeixaParticipacaoParcial() throws Exception { dados(comando(TRN+"999999/avaliar","u",notas(3)),404);assertEquals(0,total("VAT_TREINO_AVALIADO")); }
    @Test void atualizacaoInvalidaNaoModificaDados() throws Exception {
        var id=id(criar("u",notas(3)));var n=notas(4);n.put("auxilio",2.5);dados(comando(AVP+id+"/editar","u",n),400);assertEquals(3,estatistica().path("geral").asDouble());
    }
    @Test void bancoTambemGaranteUnicidadeENotas() throws Exception {
        id(criar("u",notas(3)));
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->banco.update("insert into AVP_AVALIACAO_PROFESSOR(ALU_ID,PRO_ID,AVP_AUXILIO,AVP_PONTUALIDADE,AVP_POSTURA,AVP_DIDATICA,AVP_ADEQUACAO,AVP_CRIACAO,AVP_ATUALIZACAO,TRN_ID) select ALU_ID,PRO_ID,3,3,3,3,3,AVP_CRIACAO,AVP_ATUALIZACAO,TRN_ID from AVP_AVALIACAO_PROFESSOR"));
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->banco.update("update AVP_AVALIACAO_PROFESSOR set AVP_AUXILIO=6"));
    }
    @Test void mesmoAlunoAvaliaDoisTreinosDoMesmoProfessor() throws Exception {
        id(criar("u",notas(2)));var outro=novoTreino("u",professor,true,INICIO.minusSeconds(18000),INICIO.minusSeconds(14400));
        id(comando(TRN+outro+"/avaliar","u",notas(4)));assertEquals(2,estatistica().path("quantidade").asInt());assertEquals(3,estatistica().path("geral").asDouble());
    }
    @Test void proibeAvaliacaoDiretaDoProfessor() throws Exception {
        mvc.perform(post(PRO+professor+"/avaliar").with(aluno("u")).with(csrf()).contentType("application/json").content(json.writeValueAsString(notas(5)))).andExpect(status().isNotFound());
    }
    @Test void treinoDeOutroAlunoNaoPodeSerConsultadoOuAvaliado() throws Exception {
        var id=treino("v",professor);dados(comando(TRN+id+"/avaliar","u",notas(5)),403);
        mvc.perform(get(TRN+id+"/avaliacao").with(aluno("u"))).andExpect(status().isForbidden());
        mvc.perform(get(TRN+"pesquisa").with(aluno("u"))).andExpect(jsonPath("$.resultado.total").value(0));assertEquals(0,total("VAT_TREINO_AVALIADO"));
    }
    @Test void somenteConcluidosEPassadosSaoListadosEAvaliaveis() throws Exception {
        var pendente=novoTreino("u",professor,false,INICIO.minusSeconds(7200),INICIO.minusSeconds(3600));
        var futuro=novoTreino("u",professor,true,INICIO.plusSeconds(3600),INICIO.plusSeconds(7200));
        for(String id:List.of(pendente,futuro)) dados(comando(TRN+id+"/avaliar","u",notas(4)),409);
        mvc.perform(get(TRN+"pesquisa").with(aluno("u"))).andExpect(jsonPath("$.resultado.total").value(0));
    }
    @Test void historicoRealTemDataHoraProfessorEPaginacao() throws Exception {
        for(int i=0;i<11;i++) novoTreino("u",professor,true,INICIO.minusSeconds((i+2)*3600),INICIO.minusSeconds((i+1)*3600));
        var primeiro=banco.queryForObject("select min(TRN_ID) from TRN_TREINO",Long.class).toString();
        mvc.perform(get(TRN+"pesquisa").param("tamanhoPagina","9").with(aluno("u"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.resultado.total").value(11)).andExpect(jsonPath("$.resultado.projecoes.length()").value(9))
            .andExpect(jsonPath("$.resultado.projecoes[0].identificador").value(primeiro)).andExpect(jsonPath("$.resultado.projecoes[0].professorNome").value("Ana Souza"))
            .andExpect(jsonPath("$.resultado.projecoes[0].inicio").value(INICIO.minusSeconds(7200).toString())).andExpect(jsonPath("$.resultado.projecoes[0].demonstracao").value(false));
        mvc.perform(get(TRN+"pesquisa").param("tamanhoPagina","9").param("indicePagina","1").with(aluno("u"))).andExpect(jsonPath("$.resultado.projecoes.length()").value(2));
    }
    @Test void demonstracaoNaoEstaDisponivelForaDoPerfilDesenvolvimento() throws Exception {
        mvc.perform(get(TRN+"demonstracao").with(aluno("u"))).andExpect(jsonPath("$.disponivel").value(false));
        mvc.perform(post(TRN+"demonstracao").with(aluno("u")).with(csrf())).andExpect(status().isForbidden());
    }
    @Test void corpoNaoPodeSubstituirOTreinoSelecionado() throws Exception {
        var selecionado=treino("u",professor);var outro=treino("v",segundo);var n=notas(5);n.put("treino",outro);n.put("professor",segundo);n.put("aluno","v");
        var a=dados(comando(TRN+selecionado+"/avaliar","u",n),200);assertEquals(selecionado,a.path("treino").asText());assertEquals(professor,a.path("professor").asText());
    }
    @TestConfiguration static class Configuracao { @Bean @Primary RelogioTeste relogioTeste() { return new RelogioTeste(); } }
    static class RelogioTeste extends Clock {
        final AtomicReference<Instant> agora=new AtomicReference<>(INICIO);
        public ZoneId getZone(){return ZoneOffset.UTC;}public Clock withZone(ZoneId zona){return this;}public Instant instant(){return agora.get();}
    }
}
