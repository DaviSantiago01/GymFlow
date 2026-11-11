package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.ExercicioConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.ExercicioOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.CategoriaExercicio;

@DataJpaTest
class ExercicioAplicacaoRepositorioTest {
    @Autowired
    private IExercicioRepositorioJpa repositorioJpa;
    private ExercicioAplicacaoRepositorio repositorio;

    @BeforeEach
    void configurar() {
        repositorio = new ExercicioAplicacaoRepositorio(repositorioJpa);
        repositorioJpa.save(new ExercicioJpa(null, "Supino reto", CategoriaExercicio.PEITO, new BigDecimal("200.00"), null));
        repositorioJpa.save(new ExercicioJpa(null, "Agachamento", CategoriaExercicio.PERNAS, new BigDecimal("300.00"), null));
    }

    private static ExercicioConsulta consultar(ExercicioOrdenacao ordenacao, Optional<String> nome) {
        return new ExercicioConsulta(Optional.of(ordenacao), Optional.empty(), Optional.empty(), Optional.empty(), nome);
    }

    @Test
    void pesquisaOrdenadaPorNome() {
        var resultado = repositorio.pesquisar(consultar(ExercicioOrdenacao.NOME, Optional.empty()));
        assertEquals(2, resultado.getTotal());
        assertEquals("Agachamento", resultado.getProjecoes().get(0).nome());
    }

    @Test
    void pesquisaFiltrandoPorTrechoDoNome() {
        var resultado = repositorio.pesquisar(consultar(ExercicioOrdenacao.NOME, Optional.of("supino")));
        assertEquals(1, resultado.getTotal());
        assertEquals(CategoriaExercicio.PEITO, resultado.getProjecoes().get(0).categoria());
    }
}
