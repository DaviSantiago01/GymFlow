package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

/**
 * RN011: variação de um indicador em relação ao período anterior equivalente. Sem valor anterior, ou com valor anterior
 * igual a zero, a variação percentual não é calculada.
 */
public record Variacao(BigDecimal absoluta, Optional<BigDecimal> percentual, ResultadoComparacao resultado)
        implements IObjetoValor {
    private static final BigDecimal CEM = new BigDecimal("100");

    public Variacao {
        Objects.requireNonNull(absoluta);
        Objects.requireNonNull(percentual);
        Objects.requireNonNull(resultado);
    }

    public static Variacao calcular(BigDecimal atual, Optional<BigDecimal> anterior, SentidoIndicador sentido) {
        Objects.requireNonNull(atual);
        Objects.requireNonNull(anterior);
        Objects.requireNonNull(sentido);
        if (anterior.isEmpty()) {
            return new Variacao(BigDecimal.ZERO, Optional.empty(), ResultadoComparacao.SEM_BASE);
        }
        var base = anterior.get();
        var absoluta = atual.subtract(base);
        Optional<BigDecimal> percentual = base.signum() == 0 ? Optional.empty()
                : Optional.of(absoluta.multiply(CEM).divide(base.abs(), 1, RoundingMode.HALF_UP));
        ResultadoComparacao resultado;
        if (base.signum() == 0 && atual.signum() != 0) {
            resultado = ResultadoComparacao.SEM_BASE;
        } else if (absoluta.signum() == 0) {
            resultado = ResultadoComparacao.ESTAVEL;
        } else {
            resultado = sentido.melhorou(base, atual) ? ResultadoComparacao.MELHOROU : ResultadoComparacao.PIOROU;
        }
        return new Variacao(absoluta, percentual, resultado);
    }

    public enum ResultadoComparacao {
        MELHOROU, PIOROU, ESTAVEL, SEM_BASE
    }
}
