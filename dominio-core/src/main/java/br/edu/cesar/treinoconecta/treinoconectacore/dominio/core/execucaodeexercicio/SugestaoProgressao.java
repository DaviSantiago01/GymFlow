package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio;

import java.math.BigDecimal;
import java.util.Objects;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.Exercicio;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

public record SugestaoProgressao(BigDecimal cargaAtual, BigDecimal cargaSugerida, boolean progressaoMaximaAtingida)
        implements IObjetoValor {
    public SugestaoProgressao {
        Objects.requireNonNull(cargaAtual);
        Objects.requireNonNull(cargaSugerida);
    }

    public static SugestaoProgressao calcular(BigDecimal ultimaCarga, Exercicio exercicio) {
        Objects.requireNonNull(ultimaCarga);
        Objects.requireNonNull(exercicio);
        var proximaCarga = exercicio.progredirCarga(ultimaCarga);
        if (proximaCarga.compareTo(exercicio.getCargaMaxima()) > 0) {
            return new SugestaoProgressao(ultimaCarga, exercicio.getCargaMaxima(), true);
        }
        return new SugestaoProgressao(ultimaCarga, proximaCarga, false);
    }
}
