package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva;

import java.time.*;
import java.util.Objects;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

/** Intervalo da agenda da academia, com término exclusivo. */
public record PeriodoReserva(Instant inicio, Instant termino) implements IObjetoValor {
    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    public static final int ABERTURA = 6;
    public static final int FECHAMENTO = 22;
    public PeriodoReserva {
        Objects.requireNonNull(inicio); Objects.requireNonNull(termino);
        var local = inicio.atZone(FUSO);
        var fim = termino.atZone(FUSO);
        var duracao = Duration.between(inicio, termino);
        Validate.isTrue(local.getMinute() == 0 && local.getSecond() == 0 && local.getNano() == 0,
                "Selecione um horário em hora cheia");
        Validate.isTrue(duracao.equals(Duration.ofHours(1)) || duracao.equals(Duration.ofHours(2)) || duracao.equals(Duration.ofHours(3)),
                "A duração deve ser de uma a três horas inteiras");
        Validate.isTrue(local.toLocalDate().equals(fim.toLocalDate()) && local.getHour() >= ABERTURA &&
                !fim.toLocalTime().isAfter(LocalTime.of(FECHAMENTO, 0)), "Escolha um período entre 06h e 22h");
    }
    public static PeriodoReserva criar(LocalDate data, int hora, int duracaoHoras) {
        Objects.requireNonNull(data);
        Validate.isTrue(hora >= ABERTURA && hora < FECHAMENTO, "O início deve estar entre 06h e 21h");
        Validate.isTrue(duracaoHoras >= 1 && duracaoHoras <= 3, "A duração deve ser de uma a três horas");
        Validate.isTrue(hora + duracaoHoras <= FECHAMENTO, "A reserva deve terminar até as 22h");
        var inicio = data.atTime(hora, 0).atZone(FUSO).toInstant();
        return new PeriodoReserva(inicio, inicio.plus(Duration.ofHours(duracaoHoras)));
    }
}
