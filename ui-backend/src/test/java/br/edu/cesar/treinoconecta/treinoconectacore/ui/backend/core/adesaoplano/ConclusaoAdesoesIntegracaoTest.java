package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.adesaoplano;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import br.edu.cesar.treinoconecta.treinoconectacore.BackendAplicacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.LimpezaBanco;

/** A rotina agendada conclui, sem requisição HTTP, a adesão cuja recorrência terminou. */
@SpringBootTest(classes = BackendAplicacao.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:conclusao-adesoes;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "treino.reservas.agendamento=false", "treino.grupos.agendamento=false", "treino.adesoes.intervalo-ms=100"})
class ConclusaoAdesoesIntegracaoTest {
    @Autowired
    private JdbcTemplate banco;

    @Test
    void rotinaConcluiAdesaoVencidaEPreservaAdesaoSemRecorrencia() throws Exception {
        LimpezaBanco.limparDependentes(banco);
        for (var tabela : new String[] {"PLA_PLANO", "USU_CREDENCIAL", "ALU_ALUNO"}) {
            banco.update("delete from " + tabela);
        }
        banco.update("insert into ALU_ALUNO(ALU_USUARIO,ALU_NOME) values ('u','Mariana'),('v','Pedro')");
        banco.update("insert into PLA_PLANO(PLA_NOME,PLA_VALOR,PLA_DURACAO_MESES,PLA_SITUACAO) values ('Mensal',99.90,1,'ATIVO')");
        var inicio = LocalDate.now(ParametrosAcademia.FUSO).minusMonths(2);
        banco.update("""
                insert into ADP_ADESAO_PLANO(ALU_ID,PLA_ID,ADP_PERIODICIDADE_MESES,ADP_INICIO,ADP_SITUACAO,ADP_QUANTIDADE_PERIODOS)
                select a.ALU_ID, p.PLA_ID, 1, ?, 'ATIVA', case when a.ALU_USUARIO = 'u' then 1 end
                  from ALU_ALUNO a, PLA_PLANO p""", inicio);

        String situacao = "ATIVA";
        for (int tentativa = 0; tentativa < 100 && situacao.equals("ATIVA"); tentativa++) {
            Thread.sleep(100);
            situacao = banco.queryForObject("""
                    select d.ADP_SITUACAO from ADP_ADESAO_PLANO d join ALU_ALUNO a on a.ALU_ID = d.ALU_ID
                     where a.ALU_USUARIO = 'u'""", String.class);
        }
        assertEquals("CONCLUIDA", situacao);
        assertEquals(inicio.plusMonths(1), banco.queryForObject("""
                select d.ADP_ENCERRAMENTO from ADP_ADESAO_PLANO d join ALU_ALUNO a on a.ALU_ID = d.ALU_ID
                 where a.ALU_USUARIO = 'u'""", LocalDate.class));
        assertEquals(1, banco.queryForObject("""
                select count(*) from MEN_MENSALIDADE m join ALU_ALUNO a on a.ALU_ID = m.ALU_ID
                 where a.ALU_USUARIO = 'u'""", Long.class));
        assertEquals("ATIVA", banco.queryForObject("""
                select d.ADP_SITUACAO from ADP_ADESAO_PLANO d join ALU_ALUNO a on a.ALU_ID = d.ALU_ID
                 where a.ALU_USUARIO = 'v'""", String.class));
    }
}
