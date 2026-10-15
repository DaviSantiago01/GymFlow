package dev.academia.sga.dominio.treino.exercicio;

import static org.apache.commons.lang3.Validate.notNull;

public class ExercicioServico {
	private final ExercicioRepositorio exercicioRepositorio;

	public ExercicioServico(ExercicioRepositorio exercicioRepositorio) {
		notNull(exercicioRepositorio, "O repositório de exercícios não pode ser nulo");

		this.exercicioRepositorio = exercicioRepositorio;
	}

	public void salvar(Exercicio exercicio) {
		notNull(exercicio, "O exercício não pode ser nulo");

		exercicioRepositorio.salvar(exercicio);
	}

	public Exercicio obter(ExercicioId id) {
		notNull(id, "O id do exercício não pode ser nulo");

		return exercicioRepositorio.obter(id);
	}
}
