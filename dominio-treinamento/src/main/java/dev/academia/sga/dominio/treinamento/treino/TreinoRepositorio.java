package dev.academia.sga.dominio.treinamento.treino;

public interface TreinoRepositorio {
	void salvar(Treino treino);

	Treino obter(TreinoId id);
}
