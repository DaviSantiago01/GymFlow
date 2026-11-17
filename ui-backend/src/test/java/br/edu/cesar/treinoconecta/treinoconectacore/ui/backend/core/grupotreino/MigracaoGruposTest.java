package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.grupotreino;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MigracaoGruposTest {
 @Test void adicionaAcademiaSemPerderAlunoReservaOuTreino()throws Exception{
  try(var fonte=new HikariDataSource()){fonte.setJdbcUrl("jdbc:h2:mem:migracao-grupos;DB_CLOSE_DELAY=-1");fonte.setUsername("sa");fonte.setPassword("");Flyway.configure().dataSource(fonte).locations("classpath:db/migration").target("5").load().migrate();
   try(var c=fonte.getConnection();var sql=c.createStatement()){
    sql.executeUpdate("insert into ALU_ALUNO(ALU_ID,ALU_USUARIO,ALU_NOME) values(1,'antigo','Aluno antigo')");
    sql.executeUpdate("insert into ARM_ARMARIO values('A-01','Local','Descrição')");
    sql.executeUpdate("insert into RSV_RESERVA_ARMARIO(ALU_ID,ARM_CODIGO,RSV_CONFIRMACAO,RSV_INICIO,RSV_TERMINO,RSV_SITUACAO) values(1,'A-01','2026-01-01T06:00:00Z','2026-01-01T09:00:00Z','2026-01-01T10:00:00Z','ATIVA')");
    sql.executeUpdate("insert into PRO_PROFESSOR(PRO_ID,PRO_NOME,PRO_ESPECIALIDADE,PRO_BIOGRAFIA) values(1,'Professor','Musculação','Perfil')");
    sql.executeUpdate("insert into TRN_TREINO(ALU_ID,PRO_ID,TRN_DESCRICAO,TRN_INICIO,TRN_TERMINO,TRN_CONCLUIDO) values(1,1,'Treino real','2026-01-01T09:00:00Z','2026-01-01T10:00:00Z',true)");
    Flyway.configure().dataSource(fonte).locations("classpath:db/migration").load().migrate();
    try(var r=sql.executeQuery("select ALU_NOME,ACD_ID from ALU_ALUNO")){assertTrue(r.next());assertEquals("Aluno antigo",r.getString(1));assertEquals(1,r.getLong(2));}
    for(String tabela:java.util.List.of("RSV_RESERVA_ARMARIO","TRN_TREINO"))try(var r=sql.executeQuery("select count(*) from "+tabela)){assertTrue(r.next());assertEquals(1,r.getInt(1));}
    try(var r=sql.executeQuery("select count(*) from GRT_GRUPO_TREINO")){assertTrue(r.next());assertEquals(0,r.getInt(1));}
   }
  }
 }
}
