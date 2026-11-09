package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.CategoriaExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.Exercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import dev.sauloaraujo.d5.framework.dominio.AgregadoInexistenteExcecao;

@DataJpaTest
class ExercicioRepositorioTest {
    @Autowired
    private IExercicioRepositorioJpa repositorioJpa;
    private ExercicioRepositorio repositorio;

    @BeforeEach
    void configurar() {
        repositorio = new ExercicioRepositorio(repositorioJpa);
    }

    @Test
    void incluiEBuscaPreservandoIncremento() {
        var incluido = repositorio.incluir(new Exercicio(Optional.empty(), "Supino", CategoriaExercicio.PEITO,
                new BigDecimal("200.00"), Optional.of(new BigDecimal("2.50"))));
        var encontrado = repositorio.buscarPorIdentificador(incluido.getIdentificador().orElseThrow());
        assertEquals("Supino", encontrado.getNome());
        assertEquals(CategoriaExercicio.PEITO, encontrado.getCategoria());
        assertEquals(0, new BigDecimal("2.50").compareTo(encontrado.getIncrementoCarga().orElseThrow()));
    }

    @Test
    void incrementoAusentePermaneceVazio() {
        var incluido = repositorio.incluir(new Exercicio(Optional.empty(), "Crucifixo", CategoriaExercicio.PEITO,
                new BigDecimal("80.00"), Optional.empty()));
        assertTrue(repositorio.buscarPorIdentificador(incluido.getIdentificador().orElseThrow())
                .getIncrementoCarga().isEmpty());
    }

    @Test
    void identificadorInexistente() {
        var identificador = new ExercicioIdentificador("999");
        assertFalse(repositorio.existePorIdentificador(identificador));
        assertThrows(AgregadoInexistenteExcecao.class, () -> repositorio.buscarPorIdentificador(identificador));
    }
}
