package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.grupotreino;
import java.time.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import br.edu.cesar.treinoconecta.treinoconectacore.BackendAplicacao;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(classes=BackendAplicacao.class,properties={"spring.datasource.url=jdbc:h2:mem:expiracao-grupos;DB_CLOSE_DELAY=-1","treino.reservas.agendamento=false","treino.grupos.intervalo-ms=50"})
class ExpiracaoGruposIntegracaoTest {
 @Autowired JdbcTemplate banco;
 @Test void expiraAutomaticamenteSemQualquerRequisicaoHttp(){
  banco.update("insert into ALU_ALUNO(ALU_ID,ALU_USUARIO,ALU_NOME) values(900,'scheduler','Aluno do scheduler'),(901,'confirmado','Aluno confirmado')");
  banco.update("insert into GRT_GRUPO_TREINO(GRT_ID,GRT_NOME,ALU_ORGANIZADOR,ACD_ID,GRT_CRIACAO) values(900,'Grupo automático',900,1,'2020-01-01T00:00:00Z')");
  banco.update("insert into PGR_PARTICIPACAO_GRUPO(GRT_ID,ALU_ID,ACD_ID,PGR_ENTRADA) values(900,900,1,'2020-01-01T00:00:00Z'),(900,901,1,'2020-01-01T00:00:00Z')");
  banco.update("insert into TGR_TREINO_GRUPO(TGR_ID,GRT_ID,TGR_INICIO,TGR_PRAZO,TGR_CRIACAO) values(900,900,'2020-01-01T03:00:00Z','2020-01-01T02:00:00Z','2020-01-01T00:00:00Z')");
  banco.update("insert into CTR_CONVITE_TREINO(TGR_ID,ALU_ID,CTR_SITUACAO,CTR_CRIACAO,CTR_RESPOSTA) values(900,900,'PENDENTE','2020-01-01T00:00:00Z',null),(900,901,'CONFIRMADO','2020-01-01T00:00:00Z','2020-01-01T01:00:00Z')");
  await().atMost(Duration.ofSeconds(5)).untilAsserted(()->assertEquals("EXPIRADO",banco.queryForObject("select CTR_SITUACAO from CTR_CONVITE_TREINO where ALU_ID=900",String.class)));
  assertEquals("CONFIRMADO",banco.queryForObject("select CTR_SITUACAO from CTR_CONVITE_TREINO where ALU_ID=901",String.class));
 }
}
