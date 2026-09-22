package dev.academia.sga.aplicacao.administracao.aluno;

import java.util.List;

public interface AlunoRepositorioAplicacao {
	List<AlunoResumo> pesquisarResumos();

	List<AlunoResumoExpandido> pesquisarResumosExpandidos();
}
