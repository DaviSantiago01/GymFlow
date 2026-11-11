package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.planodetreino;

import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.Treino;

public record RevisaoPlanoRequisicao(String nome, int frequenciaSemanal, List<Treino> treinos) { }
