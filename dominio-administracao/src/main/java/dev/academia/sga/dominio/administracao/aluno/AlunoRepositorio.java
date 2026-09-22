package dev.academia.sga.dominio.administracao.aluno;

public interface AlunoRepositorio {
	void salvar(Aluno aluno);

	Aluno obter(AlunoId id);
}
