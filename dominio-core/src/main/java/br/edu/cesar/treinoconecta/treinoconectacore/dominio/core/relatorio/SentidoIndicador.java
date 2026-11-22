package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

import java.math.BigDecimal;
import java.util.Objects;

/** RN016: em alguns indicadores um valor maior é melhor; em outros, como evasão e inadimplência, é o contrário. */
public enum SentidoIndicador {
    MAIOR_MELHOR, MENOR_MELHOR;

    public boolean atinge(BigDecimal realizado, BigDecimal desejado) {
        Objects.requireNonNull(realizado);
        Objects.requireNonNull(desejado);
        int comparacao = realizado.compareTo(desejado);
        return this == MAIOR_MELHOR ? comparacao >= 0 : comparacao <= 0;
    }

    /** Indica se a mudança do valor anterior para o atual representa melhora. Valores iguais não são melhora. */
    public boolean melhorou(BigDecimal anterior, BigDecimal atual) {
        int comparacao = atual.compareTo(anterior);
        return this == MAIOR_MELHOR ? comparacao > 0 : comparacao < 0;
    }
}
