package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

public record PontoIndicador(LocalDate data, BigDecimal valor) implements IObjetoValor {
    public PontoIndicador {
        Objects.requireNonNull(data);
        Objects.requireNonNull(valor);
    }
}
