package dev.academia.sga.dominio.treino.treino;

public interface TreinoRepositorio {
	void salvar(Treino treino);

	Treino obter(TreinoId id);
}
