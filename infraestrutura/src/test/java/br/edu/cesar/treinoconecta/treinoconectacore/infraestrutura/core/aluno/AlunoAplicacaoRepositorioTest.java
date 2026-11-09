package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoOrdenacao;

@DataJpaTest
class AlunoAplicacaoRepositorioTest {
    @Autowired
    private IAlunoConsultaJpa repositorioJpa;
    private AlunoAplicacaoRepositorio repositorio;

    @BeforeEach
    void configurar() {
        repositorio = new AlunoAplicacaoRepositorio(repositorioJpa);
        repositorioJpa.save(new AlunoJpa(null, "pedro", "Pedro Silva"));
        repositorioJpa.save(new AlunoJpa(null, "mariana", "Mariana Costa"));
    }

    private static AlunoConsulta consultar(Optional<String> nome) {
        return new AlunoConsulta(Optional.of(AlunoOrdenacao.NOME), Optional.empty(), Optional.empty(), Optional.empty(), nome);
    }

    @Test
    void pesquisaOrdenadaPorNome() {
        var resultado = repositorio.pesquisar(consultar(Optional.empty()));
        assertEquals(2, resultado.getTotal());
        assertEquals("Mariana Costa", resultado.getProjecoes().get(0).nome());
        assertEquals("mariana", resultado.getProjecoes().get(0).usuario());
    }

    @Test
    void pesquisaFiltrandoPorTrechoDoNome() {
        var resultado = repositorio.pesquisar(consultar(Optional.of("silva")));
        assertEquals(1, resultado.getTotal());
        assertEquals("Pedro Silva", resultado.getProjecoes().get(0).nome());
    }
}
