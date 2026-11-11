package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.planodetreino;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.CategoriaExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.ObjetivoTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.Serie;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.SituacaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.Treino;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio.ExercicioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio.IExercicioRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.IPersonalRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.PersonalJpa;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

@DataJpaTest
class PlanoDeTreinoAplicacaoRepositorioTest {
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
    private final ISegurancaServico seguranca = mock(ISegurancaServico.class);
    private PlanoDeTreinoRepositorio repositorio;
    private PlanoDeTreinoAplicacaoRepositorio repositorioAplicacao;
    private AlunoIdentificador mariana;
    private AlunoIdentificador pedro;
    private PersonalIdentificador personal;
    private ExercicioIdentificador supino;

    @BeforeEach
    void configurar() {
        repositorio = new PlanoDeTreinoRepositorio(repositorioJpa, alunosJpa, personaisJpa, exerciciosJpa);
        repositorioAplicacao = new PlanoDeTreinoAplicacaoRepositorio(repositorioJpa, exerciciosJpa, seguranca);
        mariana = criarAluno("mariana", "Mariana Costa");
        pedro = criarAluno("pedro", "Pedro Silva");
        personal = new PersonalIdentificador(personaisJpa.save(new PersonalJpa(null, "p", "Rafael")).getIdentificador().toString());
        var exercicio = exerciciosJpa.save(new ExercicioJpa(null, "Supino", CategoriaExercicio.PEITO, new BigDecimal("200.00"), null));
        supino = new ExercicioIdentificador(exercicio.getIdentificador().toString());
    }

    private AlunoIdentificador criarAluno(String usuario, String nome) {
        return new AlunoIdentificador(alunosJpa.save(new AlunoJpa(null, usuario, nome)).getIdentificador().toString());
    }

    private PlanoDeTreinoIdentificador incluir(AlunoIdentificador aluno, String nome, SituacaoPlano situacao) {
        var serie = new Serie(supino, 4, 8, 12, new BigDecimal("40"), 60);
        var treino = new Treino("A", DiaSemana.SEGUNDA, ObjetivoTreino.HIPERTROFIA, List.of(serie));
        var plano = repositorio.incluir(new PlanoDeTreino(Optional.empty(), aluno, personal, nome, 1, List.of(treino), situacao,
                1, Optional.empty(), AGORA, Optional.empty()));
        entidades.flush();
        entidades.clear();
        return plano.getIdentificador().orElseThrow();
    }

    private static PlanoDeTreinoConsulta consultar(Optional<String> aluno, Optional<SituacaoPlano> situacao) {
        return new PlanoDeTreinoConsulta(Optional.of(PlanoDeTreinoOrdenacao.NOME), Optional.empty(), Optional.empty(),
                Optional.empty(), aluno, situacao);
    }

    private void autenticarPersonal() {
        when(seguranca.possuiAutorizacao(Autorizacao.USUARIO.name())).thenReturn(false);
    }

    private void autenticarAluno(String usuario) {
        when(seguranca.possuiAutorizacao(Autorizacao.USUARIO.name())).thenReturn(true);
        when(seguranca.getUsuario()).thenReturn(usuario);
    }

    @Test
    void personalPesquisaOsPlanosDeTodosOsAlunos() {
        incluir(mariana, "Plano A", SituacaoPlano.RASCUNHO);
        incluir(pedro, "Plano B", SituacaoPlano.ATIVO);
        autenticarPersonal();

        var resultado = repositorioAplicacao.pesquisar(consultar(Optional.empty(), Optional.empty()));

        assertEquals(2, resultado.getTotal());
        var primeira = resultado.getProjecoes().get(0);
        assertEquals("Plano A", primeira.nome());
        assertEquals("Mariana Costa", primeira.alunoNome());
        assertEquals("Rafael", primeira.personalNome());
    }

    @Test
    void alunoSoEnxergaOsProprios() {
        incluir(mariana, "Plano A", SituacaoPlano.RASCUNHO);
        incluir(pedro, "Plano B", SituacaoPlano.ATIVO);
        autenticarAluno("pedro");

        var resultado = repositorioAplicacao.pesquisar(consultar(Optional.empty(), Optional.empty()));

        assertEquals(1, resultado.getTotal());
        assertEquals("Plano B", resultado.getProjecoes().get(0).nome());
    }

    @Test
    void filtraPorAlunoESituacao() {
        incluir(mariana, "Plano A", SituacaoPlano.RASCUNHO);
        incluir(mariana, "Plano B", SituacaoPlano.ATIVO);
        autenticarPersonal();

        var resultado = repositorioAplicacao.pesquisar(consultar(Optional.of(mariana.toString()), Optional.of(SituacaoPlano.ATIVO)));

        assertEquals(1, resultado.getTotal());
        assertEquals("Plano B", resultado.getProjecoes().get(0).nome());
    }

    @Test
    void detalheTrazNomesDosExercicios() {
        var identificador = incluir(mariana, "Plano A", SituacaoPlano.RASCUNHO);

        var detalhe = repositorioAplicacao.detalhe(identificador, true);

        assertTrue(detalhe.gerenciavel());
        assertEquals("Plano A", detalhe.plano().nome());
        assertEquals("Supino", detalhe.treinos().get(0).series().get(0).exercicioNome());
        assertTrue(detalhe.planoAnterior().isEmpty());
    }

    @Test
    void detalheDePlanoInexistente() {
        assertThrows(NoSuchElementException.class,
                () -> repositorioAplicacao.detalhe(new PlanoDeTreinoIdentificador("999"), false));
    }
}
