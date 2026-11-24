package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino;

import static br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Profissional;

/**
 * Horário fixo de um treino de grupo: começa em hora cheia, dura uma hora e fica entre o primeiro e o último horário
 * oferecidos pela academia, no fuso da academia.
 */
public record HorarioTreinoGrupo(Instant inicio) {
    public static final String HORARIO_INVALIDO = "Escolha um dos horários oferecidos: em hora cheia, das %dh às %dh."
            .formatted(PRIMEIRO_HORARIO_TREINO_GRUPO, ULTIMO_HORARIO_TREINO_GRUPO);
    public static final Duration DURACAO = Duration.ofMinutes(DURACAO_TREINO_GRUPO_MINUTOS);

    public HorarioTreinoGrupo {
        Objects.requireNonNull(inicio);
        var local = inicio.atZone(FUSO);
        Validate.isTrue(local.getMinute() == 0 && local.getSecond() == 0 && local.getNano() == 0
                && local.getHour() >= PRIMEIRO_HORARIO_TREINO_GRUPO && local.getHour() <= ULTIMO_HORARIO_TREINO_GRUPO,
                HORARIO_INVALIDO);
    }

    /** Todos os horários oferecidos em uma data, em ordem. */
    public static List<HorarioTreinoGrupo> doDia(LocalDate data) {
        Objects.requireNonNull(data);
        return IntStream.rangeClosed(PRIMEIRO_HORARIO_TREINO_GRUPO, ULTIMO_HORARIO_TREINO_GRUPO)
                .mapToObj(hora -> new HorarioTreinoGrupo(ZonedDateTime.of(data, LocalTime.of(hora, 0), FUSO).toInstant()))
                .toList();
    }

    public Instant fim() {
        return inicio.plus(DURACAO);
    }

    public LocalDate data() {
        return inicio.atZone(FUSO).toLocalDate();
    }

    public LocalTime horaInicio() {
        return inicio.atZone(FUSO).toLocalTime();
    }

    public LocalTime horaFim() {
        return fim().atZone(FUSO).toLocalTime();
    }

    /** O horário ainda aceita propostas enquanto a confirmação dos membros não encerrar (uma hora antes). */
    public boolean aceitaProposta(Instant agora) {
        return inicio.isAfter(agora.plusSeconds(ANTECEDENCIA_CONFIRMACAO_SEGUNDOS));
    }

    /** O professor trabalha durante todo o horário, segundo as disponibilidades semanais cadastradas pelo gestor. */
    public boolean dentroDaJornada(Profissional professor) {
        var dia = inicio.atZone(FUSO).getDayOfWeek();
        var hora = horaInicio();
        var termino = horaFim();
        return professor.getDisponibilidades().stream().anyMatch(d -> d.getDia() == dia
                && !d.getInicio().isAfter(hora) && !d.getFim().isBefore(termino));
    }
}
