package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.armario;

import java.time.Instant;
/** A consulta pública não revela aluno nem identificador da reserva. */
public record PeriodoOcupado(Instant inicio, Instant termino) { }
