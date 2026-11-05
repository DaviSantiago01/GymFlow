package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.reserva;

import com.zaxxer.hikari.HikariDataSource;
import java.time.OffsetDateTime;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MigracaoAgendaTest {
    @Test void preservaReservasAntigasEPermiteHorariosDiferentesAposMigracao() throws Exception {
        var url="jdbc:h2:mem:migracao-agenda;DB_CLOSE_DELAY=-1";
        // Mantém as conexões de migração no pool, como no ambiente Spring Boot.
        try(var fonte=new HikariDataSource()) {
        fonte.setJdbcUrl(url);fonte.setUsername("sa");fonte.setPassword("");
        Flyway.configure().dataSource(fonte).locations("classpath:db/migration").target("1").load().migrate();
        try(var conexao=fonte.getConnection();var sql=conexao.createStatement()) {
            sql.executeUpdate("insert into ALU_ALUNO values (1,'antigo','Aluno Antigo'),(2,'novo','Outro Aluno')");
            sql.executeUpdate("insert into ARM_ARMARIO values ('A-01','Vestiário','Padrão')");
            sql.executeUpdate("insert into RSV_RESERVA_ARMARIO(ALU_ID,ARM_CODIGO,RSV_CONFIRMACAO,RSV_TERMINO,RSV_SITUACAO) values (1,'A-01','2026-11-05 12:17:32+00','2026-11-05 15:17:32+00','ATIVA')");
            sql.executeUpdate("insert into RSV_RESERVA_ARMARIO(ALU_ID,ARM_CODIGO,RSV_CONFIRMACAO,RSV_TERMINO,RSV_ENCERRAMENTO,RSV_SITUACAO) values (1,'A-01','2026-11-04 12:00:00+00','2026-11-04 15:00:00+00','2026-11-04 13:00:00+00','CANCELADA')");
            Flyway.configure().dataSource(fonte).locations("classpath:db/migration").load().migrate();
            try(var registros=sql.executeQuery("select * from RSV_RESERVA_ARMARIO order by RSV_ID")) {
                assertTrue(registros.next());
                assertEquals("ATIVA",registros.getString("RSV_SITUACAO"));
                assertEquals(OffsetDateTime.parse("2026-11-05T12:17:32Z"),registros.getObject("RSV_INICIO",OffsetDateTime.class));
                assertEquals(registros.getObject("RSV_CONFIRMACAO"),registros.getObject("RSV_INICIO"));
                assertNull(registros.getObject("RSV_ENCERRAMENTO"));
                assertTrue(registros.next());assertEquals("CANCELADA",registros.getString("RSV_SITUACAO"));
                assertEquals(OffsetDateTime.parse("2026-11-04T13:00:00Z"),registros.getObject("RSV_ENCERRAMENTO",OffsetDateTime.class));
                assertFalse(registros.next());
            }
            assertEquals(1,sql.executeUpdate("insert into RSV_RESERVA_ARMARIO(ALU_ID,ARM_CODIGO,RSV_CONFIRMACAO,RSV_INICIO,RSV_TERMINO,RSV_SITUACAO) values (2,'A-01','2026-11-05 12:20:00+00','2026-11-05 16:00:00+00','2026-11-05 17:00:00+00','ATIVA')"));
        }
        }
    }
}
