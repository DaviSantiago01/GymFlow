package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.armario;

import java.time.LocalDate;
import java.util.List;
public record AgendaArmario(LocalDate data, LocalDate hoje, String fuso, List<HorarioDisponivel> horarios) { }
