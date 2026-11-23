package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.adesaoplano;

import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;

/** Sem quantidade de períodos, a cobrança é contínua até a troca ou o cancelamento. */
public record AdesaoRequisicao(AlunoIdentificador aluno, PlanoIdentificador plano, @Nullable Integer quantidadePeriodos) { }
