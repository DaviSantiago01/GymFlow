package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.avaliacaoprofessor;

import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MigracaoTreinosTest {
    @Test void preservaAvaliacoesAntigasSemInventarTreinos() throws Exception {
        try(var fonte=new HikariDataSource()) {
            fonte.setJdbcUrl("jdbc:h2:mem:migracao-treinos;DB_CLOSE_DELAY=-1");fonte.setUsername("sa");fonte.setPassword("");
            Flyway.configure().dataSource(fonte).locations("classpath:db/migration").target("4").load().migrate();
            try(var conexao=fonte.getConnection();var sql=conexao.createStatement()) {
                sql.executeUpdate("insert into ALU_ALUNO values(1,'antigo','Aluno Antigo')");
                sql.executeUpdate("insert into PRO_PROFESSOR(PRO_ID,PRO_NOME,PRO_ESPECIALIDADE,PRO_BIOGRAFIA) values(1,'Professor Real','Musculação','Perfil cadastrado')");
                sql.executeUpdate("insert into VAP_AVALIACAO_REALIZADA values(1,1)");
                sql.executeUpdate("insert into AVP_AVALIACAO_PROFESSOR(ALU_ID,PRO_ID,AVP_AUXILIO,AVP_PONTUALIDADE,AVP_POSTURA,AVP_DIDATICA,AVP_ADEQUACAO,AVP_COMENTARIO,AVP_CRIACAO,AVP_ATUALIZACAO) values(1,1,1,2,3,4,5,'Comentário preservado','2026-11-04T12:00:00Z','2026-11-04T12:00:00Z')");
                Flyway.configure().dataSource(fonte).locations("classpath:db/migration").load().migrate();
                try(var resultado=sql.executeQuery("select * from AVP_AVALIACAO_PROFESSOR")) {
                    assertTrue(resultado.next());assertNull(resultado.getObject("TRN_ID"));assertEquals("Comentário preservado",resultado.getString("AVP_COMENTARIO"));
                    assertEquals(1,resultado.getInt("AVP_AUXILIO"));assertEquals(5,resultado.getInt("AVP_ADEQUACAO"));assertFalse(resultado.next());
                }
                try(var resultado=sql.executeQuery("select count(*) from TRN_TREINO")){assertTrue(resultado.next());assertEquals(0,resultado.getInt(1));}
            }
        }
    }
}
