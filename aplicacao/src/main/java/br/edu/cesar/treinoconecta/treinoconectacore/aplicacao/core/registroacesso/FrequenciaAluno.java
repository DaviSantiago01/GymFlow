package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso;

import java.time.LocalDate;
import java.util.List;

/**
 * Frequência do aluno no período [inicio, fim] (F006, RN012): dias diferentes em que compareceu, no fuso da academia,
 * e tempo total de permanência.
 */
public record FrequenciaAluno(String aluno, LocalDate inicio, LocalDate fim, long diasNoPeriodo, long diasComparecidos,
        long visitas, long minutosTotais, long mediaMinutosPorVisita, List<PermanenciaRegistro> registros) { }
