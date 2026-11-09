package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino;

import dev.sauloaraujo.d5.framework.aplicacao.IOrdenacao;

public enum PlanoDeTreinoOrdenacao implements IOrdenacao<PlanoDeTreinoProjecao> {
    CRIACAO, NOME, ALUNO_NOME, SITUACAO
}
