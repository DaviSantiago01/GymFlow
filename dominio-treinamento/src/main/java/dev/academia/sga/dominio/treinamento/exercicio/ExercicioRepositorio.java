package dev.academia.sga.dominio.treinamento.exercicio;

public interface ExercicioRepositorio {
	void salvar(Exercicio exercicio);

	Exercicio obter(ExercicioId id);
}
