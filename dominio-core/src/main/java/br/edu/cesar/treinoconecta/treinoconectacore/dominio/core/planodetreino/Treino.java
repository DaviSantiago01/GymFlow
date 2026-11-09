package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

public record Treino(String nome, DiaSemana dia, ObjetivoTreino objetivo, List<Serie> series) implements IObjetoValor {
    public static final int TAMANHO_NOME = 60;

    public Treino {
        Validate.notBlank(nome, PlanoDeTreinoServico.NOME_TREINO_INVALIDO);
        Validate.isTrue(nome.length() <= TAMANHO_NOME, PlanoDeTreinoServico.NOME_TREINO_INVALIDO);
        Objects.requireNonNull(dia);
        Objects.requireNonNull(objetivo);
        series = List.copyOf(series);
        validarSeries(nome, objetivo, series);
    }

    private static void validarSeries(String nome, ObjetivoTreino objetivo, List<Serie> series) {
        var exercicios = new HashSet<ExercicioIdentificador>();
        for (var serie : series) {
            Validate.isTrue(exercicios.add(serie.exercicio()), PlanoDeTreinoServico.EXERCICIO_REPETIDO_MODELO,
                    serie.exercicio(), nome);
            objetivo.validarFaixa(serie);
        }
    }

    public boolean possuiExercicios() {
        return !series.isEmpty();
    }

    public Optional<Serie> buscarSerie(ExercicioIdentificador exercicio) {
        Objects.requireNonNull(exercicio);
        for (var serie : series) {
            if (serie.exercicio().equals(exercicio)) {
                return Optional.of(serie);
            }
        }
        return Optional.empty();
    }
}
