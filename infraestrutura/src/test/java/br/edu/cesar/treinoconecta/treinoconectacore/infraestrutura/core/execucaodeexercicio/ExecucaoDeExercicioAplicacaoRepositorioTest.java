package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.execucaodeexercicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExecucaoDeExercicioConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExecucaoDeExercicioOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.CategoriaExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.SituacaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio.ExercicioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio.IExercicioRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.IPersonalRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.PersonalJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.planodetreino.IPlanoDeTreinoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.planodetreino.PlanoDeTreinoJpa;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

@DataJpaTest
class ExecucaoDeExercicioAplicacaoRepositorioTest {
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
    private final ISegurancaServico seguranca = mock(ISegurancaServico.class);
    private ExecucaoDeExercicioAplicacaoRepositorio repositorio;
    private AlunoJpa mariana;
    private AlunoJpa pedro;
    private PersonalJpa personal;
    private ExercicioJpa supino;
    private ExercicioJpa remada;

    @BeforeEach
    void configurar() {
        repositorio = new ExecucaoDeExercicioAplicacaoRepositorio(repositorioJpa, seguranca);
        mariana = alunosJpa.save(new AlunoJpa(null, "mariana", "Mariana"));
        pedro = alunosJpa.save(new AlunoJpa(null, "pedro", "Pedro"));
        personal = personaisJpa.save(new PersonalJpa(null, "p", "Personal"));
        supino = exerciciosJpa.save(new ExercicioJpa(null, "Supino", CategoriaExercicio.PEITO, new BigDecimal("200.00"), null));
        remada = exerciciosJpa.save(new ExercicioJpa(null, "Remada", CategoriaExercicio.COSTAS, new BigDecimal("200.00"), null));
    }

    private void incluir(AlunoJpa aluno, ExercicioJpa exercicio, Instant data, String carga) {
        var plano = planosJpa.save(new PlanoDeTreinoJpa(null, aluno, personal, "Plano", 1, SituacaoPlano.RASCUNHO, 1, null, AGORA,
                null, new ArrayList<>()));
        repositorioJpa.save(new ExecucaoDeExercicioJpa(null, aluno, plano, exercicio, DiaSemana.SEGUNDA, data, 4, 10, new BigDecimal(carga)));
    }

    private static ExecucaoDeExercicioConsulta consultar(Optional<String> aluno, Optional<String> exercicio) {
        return new ExecucaoDeExercicioConsulta(Optional.of(ExecucaoDeExercicioOrdenacao.DATA), Optional.of(true), Optional.empty(),
                Optional.empty(), aluno, exercicio);
    }

    private static AlunoIdentificador identificador(AlunoJpa aluno) {
        return new AlunoIdentificador(aluno.getIdentificador().toString());
    }

    private static ExercicioIdentificador identificador(ExercicioJpa exercicio) {
        return new ExercicioIdentificador(exercicio.getIdentificador().toString());
    }

    @Test
    void personalFiltraPorAlunoEExercicio() {
        incluir(mariana, supino, AGORA, "40");
        incluir(mariana, remada, AGORA, "50");
        incluir(pedro, supino, AGORA, "60");
        when(seguranca.possuiAutorizacao(Autorizacao.USUARIO.name())).thenReturn(false);

        var resultado = repositorio.pesquisar(consultar(Optional.of(mariana.getIdentificador().toString()),
                Optional.of(supino.getIdentificador().toString())));

        assertEquals(1, resultado.getTotal());
        assertEquals("Supino", resultado.getProjecoes().get(0).exercicioNome());
        assertEquals("Mariana", resultado.getProjecoes().get(0).alunoNome());
    }

    @Test
    void alunoSoEnxergaAsProprias() {
        incluir(mariana, supino, AGORA, "40");
        incluir(pedro, supino, AGORA, "60");
        when(seguranca.possuiAutorizacao(Autorizacao.USUARIO.name())).thenReturn(true);
        when(seguranca.getUsuario()).thenReturn("pedro");

        var resultado = repositorio.pesquisar(consultar(Optional.empty(), Optional.empty()));

        assertEquals(1, resultado.getTotal());
        assertEquals("Pedro", resultado.getProjecoes().get(0).alunoNome());
    }

    @Test
    void listaEmOrdemCronologica() {
        incluir(mariana, supino, AGORA.plusSeconds(60), "50");
        incluir(mariana, supino, AGORA, "40");

        var execucoes = repositorio.listarCronologicamente(identificador(mariana), identificador(supino));

        assertEquals(2, execucoes.size());
        assertEquals(0, new BigDecimal("40").compareTo(execucoes.get(0).carga()));
    }

    @Test
    void listaExerciciosExecutadosSemRepetir() {
        incluir(mariana, supino, AGORA, "40");
        incluir(mariana, supino, AGORA.plusSeconds(60), "45");
        incluir(mariana, remada, AGORA, "50");

        var exercicios = repositorio.exerciciosExecutados(identificador(mariana));

        assertEquals(2, exercicios.size());
        assertEquals("Remada", exercicios.get(0).nome());
    }
}
