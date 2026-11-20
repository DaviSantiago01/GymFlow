package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno;

import java.time.LocalDate;

/**
 * Matrícula do aluno: a adesão ativa ao plano (F007). A validade é o fim do período de cobrança em curso, calculado
 * pela duração do plano contratado (F005, RN008).
 */
public record MatriculaResumo(String adesao, String plano, LocalDate inicio, LocalDate validade) { }
