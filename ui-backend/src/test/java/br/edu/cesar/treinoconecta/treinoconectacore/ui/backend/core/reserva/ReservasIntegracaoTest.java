package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.reserva;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import br.edu.cesar.treinoconecta.treinoconectacore.BackendAplicacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.spi.IReservaArmarioRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.reserva.ExpiracaoReservas;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes={BackendAplicacao.class,ReservasIntegracaoTest.Configuracao.class},properties={
    "spring.datasource.url=jdbc:h2:mem:reservas;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
    "treino.reservas.agendamento=false"})
class ReservasIntegracaoTest {
    @Autowired WebApplicationContext contexto;
    @Autowired JdbcTemplate banco;
    @Autowired PasswordEncoder codificador;
    @Autowired RelogioTeste relogio;
    @Autowired IReservaArmarioRepositorio reservas;
    @Autowired PlatformTransactionManager gerenciador;
    MockMvc mvc;
    final JsonMapper json=new JsonMapper();
    static final Instant INICIO=Instant.parse("2026-11-04T12:00:00Z");
    static final String ARM="/backend/core/armario/";
    static final String RSV="/backend/core/reserva-armario/";

    @BeforeEach void preparar() {
        mvc=MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        banco.update("delete from RSV_RESERVA_ARMARIO");banco.update("delete from USU_CREDENCIAL");
        banco.update("delete from ALU_ALUNO");banco.update("delete from ARM_ARMARIO");
        for(String u:List.of("u","v")) {
            banco.update("insert into ALU_ALUNO(ALU_USUARIO,ALU_NOME) values (?,?)",u,"Aluno "+u);
            banco.update("insert into USU_CREDENCIAL(ALU_USUARIO,USU_SENHA) values (?,?)",u,codificador.encode(u));
        }
        for(String a:List.of("A-01","A-02")) banco.update("insert into ARM_ARMARIO values (?,?,?)",a,"Vestiário A","Padrão");
        relogio.agora.set(INICIO);
    }
    org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor aluno(String nome) { return user(nome).authorities(new SimpleGrantedAuthority("USUARIO")); }
    MvcResult reservar(String usuario,String armario) throws Exception {
        var proxima = relogio.instant().atZone(ZoneId.of("America/Sao_Paulo"));
        if (proxima.getMinute()!=0 || proxima.getSecond()!=0 || proxima.getNano()!=0) proxima=proxima.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        return reservar(usuario,armario,proxima.toLocalDate().toString(),proxima.getHour(),3);
    }
    MvcResult reservar(String usuario,String armario,String data,int hora,int duracao) throws Exception {
        return mvc.perform(post(ARM+armario+"/reservar").with(aluno(usuario)).with(csrf()).contentType("application/json")
            .content(json.writeValueAsString(Map.of("data",data,"hora",hora,"duracaoHoras",duracao)))).andReturn();
    }
    org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requisicaoReserva(String armario) {
        return post(ARM+armario+"/reservar").contentType("application/json").content("{\"data\":\"2026-11-04\",\"hora\":9,\"duracaoHoras\":3}");
    }
    String identificador(MvcResult resultado) throws Exception { assertEquals(200,resultado.getResponse().getStatus(),resultado.getResponse().getContentAsString());return json.readTree(resultado.getResponse().getContentAsString()).path("dados").path("identificador").asText(); }
    long total() { return banco.queryForObject("select count(*) from RSV_RESERVA_ARMARIO",Long.class); }
    @Test void reservaDisponivelPersisteComPrazoExato() throws Exception {
        var resposta=reservar("u","A-01");identificador(resposta);
        JsonNode dados=json.readTree(resposta.getResponse().getContentAsString());
        assertEquals(INICIO.plusSeconds(10800).toString(),dados.path("dados").path("termino").asText());
        assertEquals(INICIO.toString(),dados.path("horarioServidor").asText());assertEquals(1,total());
    }
    @Test void armarioOcupadoRejeitaOutraReserva() throws Exception { identificador(reservar("u","A-01"));assertEquals(409,reservar("v","A-01").getResponse().getStatus());assertEquals(1,total()); }
    @Test void mesmoAlunoNaoReservaOutroArmario() throws Exception { identificador(reservar("u","A-01"));assertEquals(409,reservar("u","A-02").getResponse().getStatus());assertEquals(1,total()); }
    @Test void concorrenciaNoMesmoArmarioConfirmaSomenteUma() throws Exception { disputa("u","A-01","v","A-01"); }
    @Test void concorrenciaDoMesmoAlunoConfirmaSomenteUma() throws Exception { disputa("u","A-01","u","A-02"); }
    void disputa(String u1,String a1,String u2,String a2) throws Exception {
        for(int repeticao=0;repeticao<5;repeticao++) {
            banco.update("delete from RSV_RESERVA_ARMARIO");
            var prontas=new CountDownLatch(2);var largada=new CountDownLatch(1);
            try(var executor=Executors.newFixedThreadPool(2)) {
                var primeira=executor.submit(()->{prontas.countDown();largada.await();return reservar(u1,a1).getResponse().getStatus();});
                var segunda=executor.submit(()->{prontas.countDown();largada.await();return reservar(u2,a2).getResponse().getStatus();});
                assertTrue(prontas.await(5,TimeUnit.SECONDS));largada.countDown();
                var codigos=new ArrayList<>(List.of(primeira.get(20,TimeUnit.SECONDS),segunda.get(20,TimeUnit.SECONDS)));
                Collections.sort(codigos);assertEquals(List.of(200,409),codigos);assertEquals(1,total());
            }
        }
    }
    @Test void consultasRespeitamVencimentoAntesDoAgendador() throws Exception {
        var id=identificador(reservar("u","A-01"));relogio.agora.set(INICIO.plusSeconds(10800));
        mvc.perform(get(ARM+"A-01/visualizacao").with(aluno("v"))).andExpect(jsonPath("$.dados.disponivel").value(true));
        mvc.perform(get(RSV+"ativa").with(aluno("u"))).andExpect(jsonPath("$.dados").isEmpty());
        mvc.perform(get(RSV+id+"/visualizacao").with(aluno("u"))).andExpect(jsonPath("$.dados.situacao").value("VENCIDA"));
        identificador(reservar("v","A-01"));assertEquals(2,total());
    }
    @Test void tarefaDeExpiracaoPreservaHistoricoSemAcessoATela() throws Exception {
        var id=identificador(reservar("u","A-01"));relogio.agora.set(INICIO.plusSeconds(10800));
        new ExpiracaoReservas(reservas,relogio,new TransactionTemplate(gerenciador)).processar();
        assertEquals("VENCIDA",banco.queryForObject("select RSV_SITUACAO from RSV_RESERVA_ARMARIO where RSV_ID=?",String.class,Long.valueOf(id)));
        assertEquals(INICIO.plusSeconds(10800),banco.queryForObject("select RSV_ENCERRAMENTO from RSV_RESERVA_ARMARIO where RSV_ID=?",OffsetDateTime.class,Long.valueOf(id)).toInstant());
        assertEquals(1,total());
    }
    @Test void cancelarLiberaArmarioEPreservaHistorico() throws Exception { finalizarEVerificar("cancelar","CANCELADA"); }
    @Test void encerrarLiberaArmarioEPreservaHistorico() throws Exception { finalizarEVerificar("encerrar","ENCERRADA"); }
    void finalizarEVerificar(String acao,String situacao) throws Exception {
        var id=identificador(reservar("u","A-01"));relogio.agora.set(INICIO.plusSeconds(1800));
        mvc.perform(post(RSV+id+"/"+acao).with(aluno("u")).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.dados.situacao").value(situacao));
        mvc.perform(get(ARM+"A-01/visualizacao").with(aluno("u"))).andExpect(jsonPath("$.dados.disponivel").value(true));
        mvc.perform(get(RSV+"pesquisa").with(aluno("u"))).andExpect(jsonPath("$.resultado.total").value(1));
        mvc.perform(get(RSV+"pesquisa").with(aluno("v"))).andExpect(jsonPath("$.resultado.total").value(0));
        identificador(reservar("u","A-01"));assertEquals(2,total());
    }
    @Test void outroAlunoNaoConsultaNemManipulaReserva() throws Exception {
        var id=identificador(reservar("u","A-01"));
        for(String acao:List.of("cancelar","encerrar")) mvc.perform(post(RSV+id+"/"+acao).with(aluno("v")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(get(RSV+id+"/visualizacao").with(aluno("v"))).andExpect(status().isNotFound());
        mvc.perform(get(RSV+id+"/alteracao").with(aluno("v"))).andExpect(status().isForbidden());
    }
    @Test void finalizadaNaoPodeSerCanceladaOuEncerrada() throws Exception {
        var id=identificador(reservar("u","A-01"));mvc.perform(post(RSV+id+"/cancelar").with(aluno("u")).with(csrf())).andExpect(status().isOk());
        for(String acao:List.of("cancelar","encerrar"))mvc.perform(post(RSV+id+"/"+acao).with(aluno("u")).with(csrf())).andExpect(status().isConflict());
    }
    @Test void noLimiteExatoNaoPermiteCancelamento() throws Exception {
        var id=identificador(reservar("u","A-01"));relogio.agora.set(INICIO.plusSeconds(10800));
        mvc.perform(post(RSV+id+"/cancelar").with(aluno("u")).with(csrf())).andExpect(status().isConflict());
    }
    @Test void segurancaExigeSessaoCsrfEVinculoDeAluno() throws Exception {
        mvc.perform(get(ARM+"pesquisa")).andExpect(status().isUnauthorized());
        mvc.perform(post(ARM+"A-01/reservar").with(aluno("u"))).andExpect(status().isForbidden());
        mvc.perform(requisicaoReserva("A-01").with(aluno("desconhecido")).with(csrf())).andExpect(status().isForbidden());
        assertEquals(0,total());
    }
    @Test void loginRealMantemSessao() throws Exception {
        var login=mvc.perform(get("/backend/autenticacao").with(httpBasic("u","u"))).andExpect(status().isOk()).andReturn();
        var sessao=(MockHttpSession)login.getRequest().getSession(false);assertNotNull(sessao);
        mvc.perform(get(RSV+"ativa").session(sessao)).andExpect(status().isOk());
        mvc.perform(get("/backend/autenticacao").with(httpBasic("u","incorreta"))).andExpect(status().isUnauthorized());
    }
    @Test void identificadorDoCorpoNaoTrocaProprietario() throws Exception {
        var resultado=mvc.perform(post(ARM+"A-01/reservar").with(aluno("u")).with(csrf()).contentType("application/json").content("{\"data\":\"2026-11-04\",\"hora\":9,\"duracaoHoras\":3,\"aluno\":\"v\",\"termino\":\"2099-01-01T00:00:00Z\"}")).andExpect(status().isOk()).andReturn();
        var id=identificador(resultado);
        assertEquals("u",banco.queryForObject("select a.ALU_USUARIO from RSV_RESERVA_ARMARIO r join ALU_ALUNO a on a.ALU_ID=r.ALU_ID where RSV_ID=?",String.class,Long.valueOf(id)));
    }
    @Test void rollbackNaoDeixaReservaParcial() throws Exception {
        assertThrows(IllegalStateException.class,()->new TransactionTemplate(gerenciador).executeWithoutResult(estado->{
            banco.update("insert into RSV_RESERVA_ARMARIO(ALU_ID,ARM_CODIGO,RSV_CONFIRMACAO,RSV_INICIO,RSV_TERMINO,RSV_SITUACAO) select ALU_ID,'A-01',?,?,?,'ATIVA' from ALU_ALUNO where ALU_USUARIO='u'",INICIO,INICIO,INICIO.plusSeconds(10800));
            throw new IllegalStateException("Falha simulada depois da gravação");
        }));assertEquals(0,total());identificador(reservar("u","A-01"));
    }
    @Test void unicidadeNoBancoImpedeEscritaDiretaDuplicada() throws Exception {
        identificador(reservar("u","A-01"));
        for(String valores:List.of("'u','A-02'")) {
            var partes=valores.replace("'","").split(",");
            assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->banco.update("insert into RSV_RESERVA_ARMARIO(ALU_ID,ARM_CODIGO,RSV_CONFIRMACAO,RSV_INICIO,RSV_TERMINO,RSV_SITUACAO) select ALU_ID,?,?,?,?,'ATIVA' from ALU_ALUNO where ALU_USUARIO=?",partes[1],INICIO,INICIO,INICIO.plusSeconds(10800),partes[0]));
        }assertEquals(1,total());
    }
    @Test void horarioComFusoRepresentaMesmoInstante() throws Exception {
        relogio.agora.set(OffsetDateTime.parse("2026-11-04T09:00:00-03:00").toInstant());
        var resposta=reservar("u","A-01");identificador(resposta);
        assertEquals("2026-11-04T15:00:00Z",json.readTree(resposta.getResponse().getContentAsString()).path("dados").path("termino").asText());
    }
    @Test void identificadorInvalidoNaoProduzErroInterno() throws Exception {
        mvc.perform(get(RSV+"invalido/visualizacao").with(aluno("u"))).andExpect(status().isUnprocessableEntity());
        mvc.perform(requisicaoReserva("A-99").with(aluno("u")).with(csrf())).andExpect(status().isNotFound());assertEquals(0,total());
    }
    @Test void duracoesDeUmaADuasETresHorasPersistem() throws Exception {
        for(int duracao=1;duracao<=3;duracao++) {
            var resultado=reservar("u","A-01","2026-11-05",6,duracao);
            var id=identificador(resultado);
            var dados=json.readTree(resultado.getResponse().getContentAsString()).path("dados");
            assertEquals("2026-11-05T09:00:00Z",dados.path("inicio").asText());
            assertEquals(Instant.parse("2026-11-05T09:00:00Z").plusSeconds(3600L*duracao).toString(),dados.path("termino").asText());
            mvc.perform(post(RSV+id+"/cancelar").with(aluno("u")).with(csrf())).andExpect(status().isOk());
        }
    }
    @Test void agendaRejeitaSobreposicaoAceitaAdjacenciaELiberaCancelamento() throws Exception {
        var id=identificador(reservar("u","A-01","2026-11-05",9,2));
        var agenda=mvc.perform(get(ARM+"A-01/horarios?data=2026-11-05").with(aluno("v"))).andExpect(status().isOk()).andReturn();
        var horarios=json.readTree(agenda.getResponse().getContentAsString()).path("dados").path("horarios");
        assertEquals(16,horarios.size());
        assertEquals("[1,2,3]",horarios.get(0).path("duracoesDisponiveis").toString());
        assertEquals("[1]",horarios.get(2).path("duracoesDisponiveis").toString());
        assertFalse(horarios.get(3).path("disponivel").asBoolean());
        assertFalse(horarios.get(4).path("disponivel").asBoolean());
        assertEquals("[1,2,3]",horarios.get(5).path("duracoesDisponiveis").toString());
        assertEquals("[1,2]",horarios.get(14).path("duracoesDisponiveis").toString());
        assertEquals("[1]",horarios.get(15).path("duracoesDisponiveis").toString());
        for(int[] periodo:List.of(new int[]{8,2},new int[]{9,1},new int[]{10,2},new int[]{8,3}))
            assertEquals(409,reservar("v","A-01","2026-11-05",periodo[0],periodo[1]).getResponse().getStatus());
        var anterior=identificador(reservar("v","A-01","2026-11-05",8,1));
        mvc.perform(post(RSV+anterior+"/cancelar").with(aluno("v")).with(csrf())).andExpect(status().isOk());
        var posterior=identificador(reservar("v","A-01","2026-11-05",11,3));
        mvc.perform(post(RSV+posterior+"/cancelar").with(aluno("v")).with(csrf())).andExpect(status().isOk());
        mvc.perform(post(RSV+id+"/cancelar").with(aluno("u")).with(csrf())).andExpect(status().isOk());
        identificador(reservar("v","A-01","2026-11-05",9,3));
    }
    @Test void agendadaPodeCancelarMasSoEncerraAposInicio() throws Exception {
        var id=identificador(reservar("u","A-01","2026-11-05",6,1));
        mvc.perform(get(ARM+"A-01/visualizacao").with(aluno("v"))).andExpect(jsonPath("$.dados.disponivel").value(true));
        mvc.perform(get(RSV+"ativa").with(aluno("u"))).andExpect(jsonPath("$.dados.identificador").value(id));
        mvc.perform(post(RSV+id+"/encerrar").with(aluno("u")).with(csrf())).andExpect(status().isConflict());
        relogio.agora.set(Instant.parse("2026-11-05T09:00:00Z"));
        mvc.perform(get(ARM+"A-01/visualizacao").with(aluno("v"))).andExpect(jsonPath("$.dados.disponivel").value(false));
        mvc.perform(post(RSV+id+"/encerrar").with(aluno("u")).with(csrf())).andExpect(status().isOk());
    }
    @Test void agendaBloqueiaPassadoEEntradaInvalida() throws Exception {
        relogio.agora.set(INICIO.plusSeconds(1));
        var resultado=mvc.perform(get(ARM+"A-01/horarios?data=2026-11-04").with(aluno("u"))).andExpect(status().isOk()).andReturn();
        var horarios=json.readTree(resultado.getResponse().getContentAsString()).path("dados").path("horarios");
        assertFalse(horarios.get(3).path("disponivel").asBoolean());
        assertTrue(horarios.get(4).path("disponivel").asBoolean());
        for(int[] periodo:List.of(new int[]{5,1},new int[]{22,1},new int[]{21,2},new int[]{20,3},new int[]{9,1},new int[]{12,0},new int[]{12,4})) {
            int status=reservar("u","A-01","2026-11-04",periodo[0],periodo[1]).getResponse().getStatus();
            assertTrue(status==400 || status==422,"Status: "+status);
        }
        assertEquals(422,reservar("u","A-01","2026-11-03",10,1).getResponse().getStatus());
        mvc.perform(get(ARM+"A-01/horarios?data=2026-11-03").with(aluno("u"))).andExpect(status().isUnprocessableEntity());
        mvc.perform(get(ARM+"A-01/horarios?data=invalida").with(aluno("u"))).andExpect(status().isUnprocessableEntity());
        mvc.perform(post(ARM+"A-01/reservar").with(aluno("u")).with(csrf()).contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest());
        for(String campos:List.of("\"hora\":10.5,\"duracaoHoras\":1", "\"hora\":10,\"duracaoHoras\":1.5"))
            mvc.perform(post(ARM+"A-01/reservar").with(aluno("u")).with(csrf()).contentType("application/json")
                .content("{\"data\":\"2026-11-05\","+campos+"}")).andExpect(status().isBadRequest());
        assertEquals(0,total());
    }
    @Test void consultaDeAgendaExigeAutenticacaoENaoExpoeProprietario() throws Exception {
        mvc.perform(get(ARM+"A-01/horarios")).andExpect(status().isUnauthorized());
        identificador(reservar("u","A-01","2026-11-05",10,1));
        var resposta=mvc.perform(get(ARM+"A-01/horarios?data=2026-11-05").with(aluno("v"))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertFalse(resposta.contains("aluno"));
        assertFalse(resposta.contains("identificador"));
    }
    @Test void concorrenciaComSobreposicaoParcialConfirmaUma() throws Exception {
        var largada=new CountDownLatch(1);
        try(var executor=Executors.newFixedThreadPool(2)) {
            var a=executor.submit(()->{largada.await();return reservar("u","A-01","2026-11-05",10,2).getResponse().getStatus();});
            var b=executor.submit(()->{largada.await();return reservar("v","A-01","2026-11-05",11,3).getResponse().getStatus();});
            largada.countDown();
            var resultados=new ArrayList<>(List.of(a.get(20,TimeUnit.SECONDS),b.get(20,TimeUnit.SECONDS)));
            Collections.sort(resultados);assertEquals(List.of(200,409),resultados);assertEquals(1,total());
        }
    }
    @Test void concorrenciaComIntervalosAdjacentesConfirmaAmbas() throws Exception {
        var largada=new CountDownLatch(1);
        try(var executor=Executors.newFixedThreadPool(2)) {
            var a=executor.submit(()->{largada.await();return reservar("u","A-01","2026-11-05",10,2).getResponse().getStatus();});
            var b=executor.submit(()->{largada.await();return reservar("v","A-01","2026-11-05",12,3).getResponse().getStatus();});
            largada.countDown();assertEquals(200,a.get(20,TimeUnit.SECONDS));assertEquals(200,b.get(20,TimeUnit.SECONDS));assertEquals(2,total());
        }
    }
    @TestConfiguration static class Configuracao { @Bean @Primary RelogioTeste relogioTeste() { return new RelogioTeste(); } }
    static class RelogioTeste extends Clock {
        final AtomicReference<Instant> agora=new AtomicReference<>(INICIO);
        public ZoneId getZone(){return ZoneOffset.UTC;}public Clock withZone(ZoneId zona){return this;}public Instant instant(){return agora.get();}
    }
}
