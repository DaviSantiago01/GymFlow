package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.desafio;

import java.time.LocalDate;
import java.util.Objects;

public record DadosDesafio(String titulo, IndicadorDesafio indicador, VigenciaDesafio vigencia) {
    public DadosDesafio {
        Objects.requireNonNull(titulo); Objects.requireNonNull(indicador); Objects.requireNonNull(vigencia);
        if (titulo.isBlank()) throw new IllegalArgumentException("Informe o título do desafio.");
    }
    public LocalDate inicio() { return vigencia.inicio(); }
    public LocalDate fim() { return vigencia.fim(); }
}
