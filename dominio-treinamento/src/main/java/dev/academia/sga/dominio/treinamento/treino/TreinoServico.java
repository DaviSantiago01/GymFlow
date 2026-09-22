package dev.academia.sga.dominio.treinamento.treino;

import static org.apache.commons.lang3.Validate.notNull;

public class TreinoServico {
	private final TreinoRepositorio treinoRepositorio;

	public TreinoServico(TreinoRepositorio treinoRepositorio) {
		notNull(treinoRepositorio, "O repositório de treinos não pode ser nulo");

		this.treinoRepositorio = treinoRepositorio;
	}

	public void salvar(Treino treino) {
		notNull(treino, "O treino não pode ser nulo");

		treinoRepositorio.salvar(treino);
	}

	public Treino obter(TreinoId id) {
		notNull(id, "O id do treino não pode ser nulo");

		return treinoRepositorio.obter(id);
	}
}
