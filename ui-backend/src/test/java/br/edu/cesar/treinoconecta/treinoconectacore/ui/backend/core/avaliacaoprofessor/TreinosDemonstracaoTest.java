package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.avaliacaoprofessor;

import br.edu.cesar.treinoconecta.treinoconectacore.BackendAplicacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("desenvolvimento")
@SpringBootTest(classes=BackendAplicacao.class,properties={"spring.datasource.url=jdbc:h2:mem:treinos-demonstracao;DB_CLOSE_DELAY=-1","treino.reservas.agendamento=false"})
class TreinosDemonstracaoTest {
    @Autowired WebApplicationContext contexto;
    @Autowired JdbcTemplate banco;
    @Test void exemplosIdentificadosSaoIdempotentesEConvivemComTreinosReais() throws Exception {
        var mvc=MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        var usuario=user("novo_treino").authorities(new SimpleGrantedAuthority("USUARIO"));
        banco.update("insert into ALU_ALUNO(ALU_USUARIO,ALU_NOME) values('novo_treino','Aluno Novo')");
        banco.update("insert into PRO_PROFESSOR(PRO_NOME,PRO_ESPECIALIDADE,PRO_BIOGRAFIA) values('Professor Real','Treinamento','Perfil real')");
        banco.update("insert into TRN_TREINO(ALU_ID,PRO_ID,TRN_DESCRICAO,TRN_INICIO,TRN_TERMINO,TRN_CONCLUIDO) select a.ALU_ID,p.PRO_ID,'Treino real','2026-01-01T10:00:00Z','2026-01-01T11:00:00Z',true from ALU_ALUNO a,PRO_PROFESSOR p where a.ALU_USUARIO='novo_treino' and p.PRO_NOME='Professor Real'");
        mvc.perform(get("/backend/core/treino/demonstracao").with(usuario)).andExpect(jsonPath("$.disponivel").value(true));
        for(int i=0;i<2;i++) mvc.perform(post("/backend/core/treino/demonstracao").with(usuario).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(get("/backend/core/treino/pesquisa").with(usuario)).andExpect(jsonPath("$.resultado.total").value(4));
        assertEquals(3,banco.queryForObject("select count(*) from TRN_TREINO t join ALU_ALUNO a on a.ALU_ID=t.ALU_ID where a.ALU_USUARIO='novo_treino' and t.TRN_DEMONSTRACAO=true",Integer.class));
        assertEquals(1,banco.queryForObject("select count(*) from TRN_TREINO t join ALU_ALUNO a on a.ALU_ID=t.ALU_ID where a.ALU_USUARIO='novo_treino' and t.TRN_DEMONSTRACAO=false",Integer.class));
        mvc.perform(get("/backend/core/treino/pesquisa").with(user("u").authorities(new SimpleGrantedAuthority("USUARIO")))).andExpect(jsonPath("$.resultado.total").value(3));
    }
}
