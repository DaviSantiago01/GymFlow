package dev.academia.sga.dominio.matricula.aluno;

import dev.academia.sga.dominio.compartilhado.AlunoId;

public interface AlunoRepositorio {
	void salvar(Aluno aluno);

	Aluno obter(AlunoId id);
}
