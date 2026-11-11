package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
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
        for (var serie : series) {
            objetivo.validarFaixa(serie);
        }
    }

    public boolean possuiExercicios() {
        return !series.isEmpty();
    }

    /** RN003: o serviço usa estes métodos para informar o nome do exercício repetido. */
    public boolean existeExercicioRepetido() {
        var exercicios = new HashSet<ExercicioIdentificador>();
        for (var serie : series) {
            exercicios.add(serie.exercicio());
        }
        return exercicios.size() < series.size();
    }

    public ExercicioIdentificador buscarExercicioRepetido() {
        var exercicios = new HashSet<ExercicioIdentificador>();
        for (var serie : series) {
            if (!exercicios.add(serie.exercicio())) {
                return serie.exercicio();
            }
        }
        throw new NoSuchElementException(String.format(PlanoDeTreinoServico.SEM_EXERCICIO_REPETIDO_MODELO, nome));
    }

    public boolean existeSerie(ExercicioIdentificador exercicio) {
        Objects.requireNonNull(exercicio);
        for (var serie : series) {
            if (serie.exercicio().equals(exercicio)) {
                return true;
            }
        }
        return false;
    }

    public Serie buscarSerie(ExercicioIdentificador exercicio) {
        Objects.requireNonNull(exercicio);
        for (var serie : series) {
            if (serie.exercicio().equals(exercicio)) {
                return serie;
            }
        }
        throw new NoSuchElementException(String.format(PlanoDeTreinoServico.SERIE_INEXISTENTE_MODELO, exercicio, nome));
    }
}
