package dev.academia.sga.aplicacao.treinamento.treino;

import dev.academia.sga.aplicacao.administracao.aluno.AlunoResumo;
import dev.academia.sga.aplicacao.administracao.instrutor.InstrutorResumo;

public interface TreinoResumoExpandido {
	TreinoResumo getTreino();

	AlunoResumo getAluno();

	InstrutorResumo getInstrutor();

	int getTotalExercicios();
}
