package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.planodetreino;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.CategoriaExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.ObjetivoTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.Serie;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.SituacaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.Treino;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio.ExercicioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio.IExercicioRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.IPersonalRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.PersonalJpa;

@DataJpaTest
class PlanoDeTreinoRepositorioTest {
    private static final Instant AGORA = Instant.parse("2026-11-09T12:00:00Z");

    @Autowired
    private IPlanoDeTreinoRepositorioJpa repositorioJpa;
    @Autowired
    private IAlunoRepositorioJpa alunosJpa;
    @Autowired
    private IPersonalRepositorioJpa personaisJpa;
    @Autowired
    private IExercicioRepositorioJpa exerciciosJpa;
    @Autowired
    private EntityManager entidades;
    private PlanoDeTreinoRepositorio repositorio;
    private AlunoIdentificador aluno;
    private PersonalIdentificador personal;
    private ExercicioIdentificador supino;
    private ExercicioIdentificador remada;

    @BeforeEach
    void configurar() {
        repositorio = new PlanoDeTreinoRepositorio(repositorioJpa, alunosJpa, personaisJpa, exerciciosJpa);
        aluno = new AlunoIdentificador(alunosJpa.save(new AlunoJpa(null, "u", "Aluno")).getIdentificador().toString());
        personal = new PersonalIdentificador(personaisJpa.save(new PersonalJpa(null, "p", "Personal")).getIdentificador().toString());
        supino = criarExercicio("Supino");
        remada = criarExercicio("Remada");
    }

    private ExercicioIdentificador criarExercicio(String nome) {
        var salvo = exerciciosJpa.save(new ExercicioJpa(null, nome, CategoriaExercicio.PEITO, new BigDecimal("200.00"), null));
        return new ExercicioIdentificador(salvo.getIdentificador().toString());
    }

    private void limparContexto() {
        entidades.flush();
        entidades.clear();
    }

    private Serie serie(ExercicioIdentificador exercicio) {
        return new Serie(exercicio, 4, 8, 12, new BigDecimal("40"), 60);
    }

    private PlanoDeTreino plano(SituacaoPlano situacao) {
        var treino = new Treino("A", DiaSemana.SEGUNDA, ObjetivoTreino.HIPERTROFIA, List.of(serie(supino), serie(remada)));
        return new PlanoDeTreino(Optional.empty(), aluno, personal, "Plano", 1, List.of(treino), situacao, 1, Optional.empty(),
                AGORA, Optional.empty());
    }

    @Test
    void incluiEBuscaMantendoAOrdemDosTreinosESeries() {
        var treinoA = new Treino("A", DiaSemana.SEGUNDA, ObjetivoTreino.HIPERTROFIA, List.of(serie(remada), serie(supino)));
        var treinoB = new Treino("B", DiaSemana.QUARTA, ObjetivoTreino.HIPERTROFIA, List.of(serie(supino)));
        var incluido = repositorio.incluir(new PlanoDeTreino(Optional.empty(), aluno, personal, "Plano", 2,
                List.of(treinoA, treinoB), SituacaoPlano.RASCUNHO, 1, Optional.empty(), AGORA, Optional.empty()));
        limparContexto();

        var encontrado = repositorio.buscarPorIdentificador(incluido.getIdentificador().orElseThrow());

        assertEquals(aluno, encontrado.getAluno());
        assertEquals(personal, encontrado.getPersonal());
        assertEquals(2, encontrado.getTreinos().size());
        assertEquals("A", encontrado.getTreinos().get(0).nome());
        assertEquals(remada, encontrado.getTreinos().get(0).series().get(0).exercicio());
        assertEquals(supino, encontrado.getTreinos().get(0).series().get(1).exercicio());
        assertEquals(0, new BigDecimal("40").compareTo(encontrado.getTreinos().get(0).series().get(0).carga()));
    }

    @Test
    void alterarAtualizaSituacaoSemTrocarOsTreinos() {
        var incluido = repositorio.incluir(plano(SituacaoPlano.ATIVO));
        limparContexto();
        var treinosAntes = entidades.createNativeQuery("select TRE_ID from TRE_TREINO order by TRE_ID").getResultList();

        repositorio.alterar(incluido.encerrar(personal, AGORA));
        limparContexto();

        var encerrado = repositorio.buscarPorIdentificador(incluido.getIdentificador().orElseThrow());
        assertEquals(SituacaoPlano.ENCERRADO, encerrado.getSituacao());
        assertEquals(AGORA, encerrado.getEncerramento().orElseThrow());
        assertEquals(treinosAntes, entidades.createNativeQuery("select TRE_ID from TRE_TREINO order by TRE_ID").getResultList());
    }

    @Test
    void novaVersaoReferenciaAAnterior() {
        var original = repositorio.incluir(plano(SituacaoPlano.ATIVO));
        var revisado = original.revisar(personal, "Plano 2", 1, original.getTreinos(), AGORA);

        repositorio.alterar(original.substituir(personal, AGORA));
        var novaVersao = repositorio.incluir(revisado);
        limparContexto();

        var encontrada = repositorio.buscarPorIdentificador(novaVersao.getIdentificador().orElseThrow());
        assertEquals(2, encontrada.getVersao());
        assertEquals(original.getIdentificador(), encontrada.getPlanoAnterior());
        assertEquals(SituacaoPlano.SUBSTITUIDO,
                repositorio.buscarPorIdentificador(original.getIdentificador().orElseThrow()).getSituacao());
    }

    @Test
    void localizaPlanoAtivoDoAluno() {
        assertFalse(repositorio.existeAtivoPorAluno(aluno));
        var ativo = repositorio.incluir(plano(SituacaoPlano.ATIVO));
        limparContexto();
        assertTrue(repositorio.existeAtivoPorAluno(aluno));
        assertEquals(ativo.getIdentificador(), repositorio.buscarAtivoPorAluno(aluno).getIdentificador());
    }

    @Test
    void alunoSemPlanoAtivoLancaExcecao() {
        assertThrows(NoSuchElementException.class, () -> repositorio.buscarAtivoPorAluno(aluno));
    }

    @Test
    void bloqueiaPlanoExistente() {
        var incluido = repositorio.incluir(plano(SituacaoPlano.RASCUNHO));
        limparContexto();
        var bloqueado = repositorio.bloquear(incluido.getIdentificador().orElseThrow());
        assertEquals(1, bloqueado.getTreinos().size());
    }

    @Test
    void bancoImpedeDoisPlanosAtivosDoMesmoAluno() {
        repositorio.incluir(plano(SituacaoPlano.ATIVO));
        limparContexto();
        assertThrows(DataIntegrityViolationException.class, () -> {
            repositorio.incluir(plano(SituacaoPlano.ATIVO));
            repositorioJpa.flush();
        });
    }
}
