package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core;

import java.time.Instant;
import java.util.Objects;
/** O cliente ancora a contagem no horário do servidor, usando um relógio monotônico entre consultas. */
public record RespostaTemporal<T>(T dados, Instant horarioServidor) {
    public RespostaTemporal { Objects.requireNonNull(dados); Objects.requireNonNull(horarioServidor); }
}
