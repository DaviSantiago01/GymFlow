package dev.academia.sga.aplicacao.administracao.instrutor;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

public class InstrutorServicoAplicacao {
	private InstrutorRepositorioAplicacao repositorio;

	public InstrutorServicoAplicacao(InstrutorRepositorioAplicacao repositorio) {
		notNull(repositorio, "O repositório não pode ser nulo");

		this.repositorio = repositorio;
	}

	public List<InstrutorResumo> pesquisarResumos() {
		return repositorio.pesquisarResumos();
	}
}
