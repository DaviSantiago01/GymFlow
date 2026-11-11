package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.Personal;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;

@DataJpaTest
class PersonalRepositorioTest {
    @Autowired
    private IPersonalRepositorioJpa repositorioJpa;
    private PersonalRepositorio repositorio;

    @BeforeEach
    void configurar() {
        repositorio = new PersonalRepositorio(repositorioJpa);
    }

    @Test
    void incluiEBuscaPorUsuario() {
        var incluido = repositorio.incluir(new Personal(Optional.empty(), "rafael", "Rafael Souza"));
        var encontrado = repositorio.buscarPorUsuario("rafael");
        assertEquals(incluido.getIdentificador(), encontrado.getIdentificador());
        assertEquals("Rafael Souza", encontrado.getNome());
        assertTrue(repositorio.existePorIdentificador(encontrado.getIdentificador().orElseThrow()));
    }

    @Test
    void usuarioSemCadastroNaoEAutorizado() {
        assertThrows(NaoAutorizadoExcecao.class, () -> repositorio.buscarPorUsuario("inexistente"));
    }
}
