package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;

/** Entidade do profissional: um período de trabalho em um dia da semana, no intervalo [início, fim). */
public final class DisponibilidadeProfissional {
    private final Optional<Long> identificador;
    private final DayOfWeek dia;
    private final LocalTime inicio;
    private final LocalTime fim;

    public DisponibilidadeProfissional(Optional<Long> identificador, DayOfWeek dia, LocalTime inicio, LocalTime fim) {
        this.identificador = Objects.requireNonNull(identificador);
        this.dia = Validate.notNull(dia, ProfissionalServico.DISPONIBILIDADE_INCOMPLETA);
        this.inicio = Validate.notNull(inicio, ProfissionalServico.DISPONIBILIDADE_INCOMPLETA).withSecond(0).withNano(0);
        this.fim = Validate.notNull(fim, ProfissionalServico.DISPONIBILIDADE_INCOMPLETA).withSecond(0).withNano(0);
        Validate.isTrue(this.inicio.isBefore(this.fim), ProfissionalServico.HORARIO_INVERTIDO);
    }

    public static DisponibilidadeProfissional nova(DayOfWeek dia, LocalTime inicio, LocalTime fim) {
        return new DisponibilidadeProfissional(Optional.empty(), dia, inicio, fim);
    }

    public Duration duracao() {
        return Duration.between(inicio, fim);
    }

    /** Um período pode começar exatamente quando outro termina. */
    public boolean sobrepoe(DisponibilidadeProfissional outra) {
        Objects.requireNonNull(outra);
        return dia == outra.dia && inicio.isBefore(outra.fim) && outra.inicio.isBefore(fim);
    }

    public boolean possuiIdentificador(long valor) {
        return identificador.equals(Optional.of(valor));
    }

    public Optional<Long> getIdentificador() { return identificador; }
    public DayOfWeek getDia() { return dia; }
    public LocalTime getInicio() { return inicio; }
    public LocalTime getFim() { return fim; }
}
