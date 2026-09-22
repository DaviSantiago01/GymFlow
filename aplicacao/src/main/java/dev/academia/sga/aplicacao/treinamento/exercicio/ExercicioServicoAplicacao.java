package dev.academia.sga.aplicacao.treinamento.exercicio;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

public class ExercicioServicoAplicacao {
	private ExercicioRepositorioAplicacao repositorio;

	public ExercicioServicoAplicacao(ExercicioRepositorioAplicacao repositorio) {
		notNull(repositorio, "O repositório não pode ser nulo");

		this.repositorio = repositorio;
	}

	public List<ExercicioResumo> pesquisarResumos() {
		return repositorio.pesquisarResumos();
	}

	public List<ExercicioResumo> pesquisarResumosPorTreino(int treino) {
		return repositorio.pesquisarResumosPorTreino(treino);
	}
}
