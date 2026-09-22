package dev.academia.sga.aplicacao.treinamento.exercicio;

import java.util.List;

public interface ExercicioRepositorioAplicacao {
	List<ExercicioResumo> pesquisarResumos();

	List<ExercicioResumo> pesquisarResumosPorTreino(int treino);
}
