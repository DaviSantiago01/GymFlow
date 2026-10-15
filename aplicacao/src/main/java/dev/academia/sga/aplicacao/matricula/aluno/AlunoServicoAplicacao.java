package dev.academia.sga.aplicacao.matricula.aluno;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

public class AlunoServicoAplicacao {
	private AlunoRepositorioAplicacao repositorio;

	public AlunoServicoAplicacao(AlunoRepositorioAplicacao repositorio) {
		notNull(repositorio, "O repositório não pode ser nulo");

		this.repositorio = repositorio;
	}

	public List<AlunoResumo> pesquisarResumos() {
		return repositorio.pesquisarResumos();
	}
}
