package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.planodetreino;

import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.Treino;

public record PlanoDeTreinoRequisicao(AlunoIdentificador aluno, String nome, int frequenciaSemanal, List<Treino> treinos) { }
