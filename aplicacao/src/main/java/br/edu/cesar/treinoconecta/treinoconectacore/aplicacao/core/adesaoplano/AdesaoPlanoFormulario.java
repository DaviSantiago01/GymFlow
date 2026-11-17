package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano;

import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.PlanoProjecao;

/** Alunos e planos ativos disponíveis para uma nova adesão ou troca. */
public record AdesaoPlanoFormulario(List<AlunoProjecao> alunos, List<PlanoProjecao> planos) { }
