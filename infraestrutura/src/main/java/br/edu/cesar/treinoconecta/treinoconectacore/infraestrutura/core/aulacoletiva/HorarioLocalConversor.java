package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aulacoletiva;

import java.time.LocalTime;
import org.jspecify.annotations.Nullable;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Grava o horário como texto, sem a conversão de fuso que o Hibernate aplica às colunas TIME. Assim a grade de aulas e
 * a disponibilidade do aluno ficam no horário local, o mesmo usado nas consultas dos relatórios.
 */
@Converter
public class HorarioLocalConversor implements AttributeConverter<@Nullable LocalTime, @Nullable String> {
    @Override
    public @Nullable String convertToDatabaseColumn(@Nullable LocalTime horario) {
        return horario == null ? null : horario.toString();
    }

    @Override
    public @Nullable LocalTime convertToEntityAttribute(@Nullable String valor) {
        return valor == null ? null : LocalTime.parse(valor);
    }
}
