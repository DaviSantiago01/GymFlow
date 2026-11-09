package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino;

import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.ObjetivoTreino;

public record TreinoDetalhe(String nome, DiaSemana dia, ObjetivoTreino objetivo, List<SerieDetalhe> series) { }
