package dev.academia.sga.aplicacao.treinamento.treino;

import java.util.List;

public interface TreinoRepositorioAplicacao {
	List<TreinoResumo> pesquisarResumos();

	List<TreinoResumoExpandido> pesquisarResumosExpandidos();
}
