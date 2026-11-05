package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.reserva;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import br.edu.cesar.treinoconecta.treinoconectacore.BackendAplicacao;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes=BackendAplicacao.class,properties={
    "spring.datasource.url=jdbc:h2:mem:agendamento;DB_CLOSE_DELAY=-1",
    "treino.reservas.intervalo-ms=50"})
class AgendamentoIntegracaoTest {
    @Autowired JdbcTemplate banco;

    @Test void agendaEncerraReservaSemRequisicaoHttp() {
        var inicio=Instant.parse("2020-01-01T00:00:00Z");
        banco.update("insert into ALU_ALUNO(ALU_USUARIO,ALU_NOME) values ('agendamento','Aluno')");
        banco.update("insert into ARM_ARMARIO values ('B-01','Vestiário B','Padrão')");
        banco.update("insert into RSV_RESERVA_ARMARIO(ALU_ID,ARM_CODIGO,RSV_CONFIRMACAO,RSV_INICIO,RSV_TERMINO,RSV_SITUACAO) select ALU_ID,'B-01',?,?,?,'ATIVA' from ALU_ALUNO where ALU_USUARIO='agendamento'",inicio,inicio,inicio.plusSeconds(10800));
        await().atMost(Duration.ofSeconds(5)).untilAsserted(()->assertEquals("VENCIDA",
            banco.queryForObject("select RSV_SITUACAO from RSV_RESERVA_ARMARIO",String.class)));
        assertEquals(inicio.plusSeconds(10800),banco.queryForObject("select RSV_ENCERRAMENTO from RSV_RESERVA_ARMARIO",OffsetDateTime.class).toInstant());
    }
}
