package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoProjecao;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IAlunoAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<AlunoProjecao, AlunoConsulta> {
}
