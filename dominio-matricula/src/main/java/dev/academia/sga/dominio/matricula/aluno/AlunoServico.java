package dev.academia.sga.dominio.matricula.aluno;

import static org.apache.commons.lang3.Validate.notNull;

import dev.academia.sga.dominio.compartilhado.AlunoId;

public class AlunoServico {
	private final AlunoRepositorio alunoRepositorio;

	public AlunoServico(AlunoRepositorio alunoRepositorio) {
		notNull(alunoRepositorio, "O repositório de alunos não pode ser nulo");

		this.alunoRepositorio = alunoRepositorio;
	}

	public void salvar(Aluno aluno) {
		notNull(aluno, "O aluno não pode ser nulo");

		alunoRepositorio.salvar(aluno);
	}

	public Aluno obter(AlunoId id) {
		notNull(id, "O id do aluno não pode ser nulo");

		return alunoRepositorio.obter(id);
	}
}
