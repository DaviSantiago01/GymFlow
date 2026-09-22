package dev.academia.sga.aplicacao.treinamento.treino;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

public class TreinoServicoAplicacao {
	private TreinoRepositorioAplicacao repositorio;

	public TreinoServicoAplicacao(TreinoRepositorioAplicacao repositorio) {
		notNull(repositorio, "O repositório não pode ser nulo");

		this.repositorio = repositorio;
	}

	public List<TreinoResumo> pesquisarResumos() {
		return repositorio.pesquisarResumos();
	}

	public List<TreinoResumoExpandido> pesquisarResumosExpandidos() {
		return repositorio.pesquisarResumosExpandidos();
	}
}
