package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.ObjetivoTreino;

public record FaixaObjetivo(ObjetivoTreino objetivo, int seriesMinimas, int seriesMaximas, int repeticoesMinimas,
        int repeticoesMaximas) { }
