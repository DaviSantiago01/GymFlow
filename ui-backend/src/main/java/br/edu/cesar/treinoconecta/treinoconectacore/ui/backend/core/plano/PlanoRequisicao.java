package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.plano;

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

public record PlanoRequisicao(@Nullable String nome, @Nullable BigDecimal valor, @Nullable Integer duracaoMeses) { }
