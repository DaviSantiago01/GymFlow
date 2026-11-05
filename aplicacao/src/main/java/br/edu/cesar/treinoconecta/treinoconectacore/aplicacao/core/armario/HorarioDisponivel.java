package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.armario;

import java.time.Instant;
import java.util.List;
public record HorarioDisponivel(int hora, Instant inicio, Instant termino, boolean disponivel,
        List<Integer> duracoesDisponiveis) { }
