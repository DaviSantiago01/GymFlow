package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.spi.IAlunoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.Aluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoServico;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class AlunoAplicacaoServico extends
        AgregadoAplicacaoServicoAbstrato<AlunoIdentificador, Aluno, AgregadoAutorizacoes<Aluno>, AlunoProjecao, AlunoConsulta> {

    public AlunoAplicacaoServico(IAlunoAplicacaoRepositorio repositorio, AlunoServico servico) {
        super(repositorio, servico);
    }
}
