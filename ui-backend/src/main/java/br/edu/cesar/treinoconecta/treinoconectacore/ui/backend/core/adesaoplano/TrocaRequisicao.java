package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.adesaoplano;

import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;

public record TrocaRequisicao(PlanoIdentificador plano, @Nullable Integer quantidadePeriodos) { }
