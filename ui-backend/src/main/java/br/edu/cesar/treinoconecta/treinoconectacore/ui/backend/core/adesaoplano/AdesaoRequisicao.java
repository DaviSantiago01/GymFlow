package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.adesaoplano;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;

public record AdesaoRequisicao(AlunoIdentificador aluno, PlanoIdentificador plano) { }
