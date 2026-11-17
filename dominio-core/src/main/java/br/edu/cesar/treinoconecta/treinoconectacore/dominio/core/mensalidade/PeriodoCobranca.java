package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade;

import java.time.LocalDate;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

/** Período de cobrança com datas inclusivas. */
public record PeriodoCobranca(LocalDate inicio, LocalDate fim) {
    public PeriodoCobranca {
        Objects.requireNonNull(inicio);
        Objects.requireNonNull(fim);
        Validate.isTrue(!fim.isBefore(inicio), "O fim do período de cobrança não pode anteceder o início");
    }
}
