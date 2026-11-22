package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.aulasdesafios;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aula.spi.IAulaAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.desafio.spi.IDesafioAplicacaoRepositorio;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class AulasDesafiosLeiturasIntegracaoTest {
 @Autowired private IAulaAplicacaoRepositorio consultasAulas;
 @Autowired private IDesafioAplicacaoRepositorio consultasDesafios;
 @Test void consultasAulasDesafiosExecutamSobreAsTabelasDaV10(){
  assertThat(consultasAulas.aulas(Optional.empty(),Optional.empty(),Optional.empty(),"sem-aluno")).isEmpty();
  assertThat(consultasAulas.aula(Long.MAX_VALUE,"sem-aluno")).isEmpty();
  assertThat(consultasAulas.minhasAulas("sem-aluno")).isEmpty();
  assertThat(consultasAulas.inscritos(Long.MAX_VALUE)).isEmpty();
  assertThat(consultasAulas.notificacoes("sem-aluno")).isEmpty();
  assertThat(consultasAulas.professores()).isEmpty();
  assertThat(consultasDesafios.alunos()).isEmpty();
  assertThat(consultasDesafios.desafios()).isEmpty();
  assertThat(consultasDesafios.desafio(Long.MAX_VALUE)).isEmpty();
  assertThat(consultasDesafios.avaliacoes(Long.MAX_VALUE)).isEmpty();
  assertThat(consultasDesafios.evolucao(Long.MAX_VALUE,"sem-aluno")).isEmpty();
 }
}
