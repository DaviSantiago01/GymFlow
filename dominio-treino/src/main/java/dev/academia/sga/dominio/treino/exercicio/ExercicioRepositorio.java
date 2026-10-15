package dev.academia.sga.dominio.treino.exercicio;

public interface ExercicioRepositorio {
	void salvar(Exercicio exercicio);

	Exercicio obter(ExercicioId id);
}
