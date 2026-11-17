package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.profissional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import org.jspecify.annotations.Nullable;

public record DisponibilidadeRequisicao(@Nullable DayOfWeek dia, @Nullable LocalTime inicio, @Nullable LocalTime fim) { }
