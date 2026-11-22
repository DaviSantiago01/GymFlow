package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aula;

import java.time.LocalTime;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

/** Intervalo de uma turma; o fim precisa ser posterior ao início. */
public record PeriodoAula(LocalTime inicio, LocalTime fim) {
    public PeriodoAula {
        Objects.requireNonNull(inicio);
        Objects.requireNonNull(fim);
        Validate.isTrue(inicio.isBefore(fim), "O término da aula deve ser posterior ao início.");
    }

    public boolean sobrepoe(PeriodoAula outro) {
        Objects.requireNonNull(outro);
        return inicio.isBefore(outro.fim) && outro.inicio.isBefore(fim);
    }
}
