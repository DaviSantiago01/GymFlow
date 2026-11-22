package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao;

import java.math.BigDecimal;
import java.util.Objects;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.Serie;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

/** Troca de um exercício estagnado por outro do mesmo grupo muscular, com a prescrição sugerida. */
public record SugestaoSubstituicao(ExercicioIdentificador exercicioAtual, ExercicioIdentificador exercicioSugerido,
        int series, int repeticoesMinimas, int repeticoesMaximas, BigDecimal carga, int intervaloSegundos)
        implements IObjetoValor {
    public SugestaoSubstituicao {
        Objects.requireNonNull(exercicioAtual);
        Objects.requireNonNull(exercicioSugerido);
        Validate.isTrue(!exercicioAtual.equals(exercicioSugerido), RecomendacaoServico.MESMO_EXERCICIO);
        serie(exercicioSugerido, series, repeticoesMinimas, repeticoesMaximas, carga, intervaloSegundos);
    }

    /** Valida a prescrição com as mesmas regras de uma série do plano de treino. */
    public Serie paraSerie() {
        return serie(exercicioSugerido, series, repeticoesMinimas, repeticoesMaximas, carga, intervaloSegundos);
    }

    private static Serie serie(ExercicioIdentificador exercicio, int series, int minimas, int maximas, BigDecimal carga,
            int intervalo) {
        return new Serie(exercicio, series, minimas, maximas, carga, intervalo);
    }
}
