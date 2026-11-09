package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio;

import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.ExercicioConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.ExercicioOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.ExercicioProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.spi.IExercicioAplicacaoRepositorio;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class ExercicioAplicacaoRepositorio
        extends AgregadoAplicacaoRepositorioAbstrato<ExercicioProjecao, ExercicioOrdenacao, ExercicioConsulta>
        implements IExercicioAplicacaoRepositorio {
    private final IExercicioRepositorioJpa repositorioJpa;

    public ExercicioAplicacaoRepositorio(IExercicioRepositorioJpa repositorioJpa) {
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
    }

    @Override
    protected Page<ExercicioProjecao> pesquisar(ExercicioConsulta consulta, Pageable pageable) {
        return repositorioJpa.pesquisar(paraLike(consulta.getNome()), pageable);
    }

    @Override
    protected String paraPropriedadeOrdenacao(ExercicioOrdenacao ordenacao) {
        return switch (ordenacao) {
        case NOME -> "nome";
        case CATEGORIA -> "categoria";
        };
    }
}
