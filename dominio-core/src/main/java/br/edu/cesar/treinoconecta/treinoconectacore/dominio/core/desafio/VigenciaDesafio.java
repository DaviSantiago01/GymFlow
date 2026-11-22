package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.desafio;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

/** Regras temporais que definem inscrição e consolidação dos desafios. */
public record VigenciaDesafio(LocalDate inicio, LocalDate fim) {
    public VigenciaDesafio {
        Objects.requireNonNull(inicio);
        Objects.requireNonNull(fim);
        Validate.isTrue(!fim.isBefore(inicio.plusDays(7)), "A vigência do desafio deve ter no mínimo 7 dias.");
    }

    public boolean aceitaInscricao(LocalDate data) {
        Objects.requireNonNull(data);
        return !data.isAfter(inicio.plusDays(5)) && !data.isAfter(fim);
    }

    public static void validarMeta(java.math.BigDecimal meta) {
        Validate.isTrue(meta != null && meta.signum() > 0, "A meta deve ser maior que zero.");
    }

    public static boolean avaliacaoEditavel(OffsetDateTime registro, OffsetDateTime agora) {
        Objects.requireNonNull(registro);
        Objects.requireNonNull(agora);
        return !registro.isBefore(agora.minusHours(48));
    }
}
