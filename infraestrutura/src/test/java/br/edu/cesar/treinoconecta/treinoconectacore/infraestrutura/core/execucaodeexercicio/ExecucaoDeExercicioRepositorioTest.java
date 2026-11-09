package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.execucaodeexercicio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.CategoriaExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.SituacaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio.ExercicioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio.IExercicioRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.IPersonalRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.PersonalJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.planodetreino.IPlanoDeTreinoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.planodetreino.PlanoDeTreinoJpa;

@DataJpaTest
class ExecucaoDeExercicioRepositorioTest {
    private static final Instant AGORA = Instant.parse("2026-11-09T12:00:00Z");

    @Autowired
    private IExecucaoDeExercicioRepositorioJpa repositorioJpa;
    @Autowired
    private IPlanoDeTreinoRepositorioJpa planosJpa;
    @Autowired
    private IAlunoRepositorioJpa alunosJpa;
    @Autowired
    private IPersonalRepositorioJpa personaisJpa;
    @Autowired
    private IExercicioRepositorioJpa exerciciosJpa;
    @Autowired
    private EntityManager entidades;
    private ExecucaoDeExercicioRepositorio repositorio;
    private AlunoIdentificador aluno;
    private PlanoDeTreinoIdentificador plano;
    private ExercicioIdentificador exercicio;

    @BeforeEach
    void configurar() {
        repositorio = new ExecucaoDeExercicioRepositorio(repositorioJpa, alunosJpa, planosJpa, exerciciosJpa);
        var alunoJpa = alunosJpa.save(new AlunoJpa(null, "u", "Aluno"));
        var personalJpa = personaisJpa.save(new PersonalJpa(null, "p", "Personal"));
        var exercicioJpa = exerciciosJpa.save(new ExercicioJpa(null, "Supino", CategoriaExercicio.PEITO, new BigDecimal("200.00"), null));
        var planoJpa = planosJpa.save(new PlanoDeTreinoJpa(null, alunoJpa, personalJpa, "Plano", 1, SituacaoPlano.ATIVO, 1, null,
                AGORA, null, new ArrayList<>()));
        aluno = new AlunoIdentificador(alunoJpa.getIdentificador().toString());
        exercicio = new ExercicioIdentificador(exercicioJpa.getIdentificador().toString());
        plano = new PlanoDeTreinoIdentificador(planoJpa.getIdentificador().toString());
    }

    private ExecucaoDeExercicio incluir(Instant data, String carga) {
        var execucao = repositorio.incluir(new ExecucaoDeExercicio(Optional.empty(), aluno, plano, DiaSemana.SEGUNDA, exercicio, data,
                4, 10, new BigDecimal(carga)));
        entidades.flush();
        entidades.clear();
        return execucao;
    }

    @Test
    void incluiEBuscaOsDadosDaExecucao() {
        var incluida = incluir(AGORA, "42.5");
        var encontrada = repositorio.buscarPorIdentificador(incluida.getIdentificador().orElseThrow());
        assertEquals(aluno, encontrada.getAluno());
        assertEquals(plano, encontrada.getPlano());
        assertEquals(exercicio, encontrada.getExercicio());
        assertEquals(DiaSemana.SEGUNDA, encontrada.getDia());
        assertEquals(AGORA, encontrada.getData());
        assertEquals(0, new BigDecimal("42.5").compareTo(encontrada.getCarga()));
    }

    @Test
    void buscaHistoricoEmOrdemCronologica() {
        incluir(AGORA.plusSeconds(3600), "50");
        incluir(AGORA, "40");
        var historico = repositorio.buscarPorAlunoEExercicio(aluno, exercicio);
        assertEquals(2, historico.size());
        assertEquals(0, new BigDecimal("40").compareTo(historico.get(0).getCarga()));
        assertEquals(0, new BigDecimal("50").compareTo(historico.get(1).getCarga()));
    }
}
