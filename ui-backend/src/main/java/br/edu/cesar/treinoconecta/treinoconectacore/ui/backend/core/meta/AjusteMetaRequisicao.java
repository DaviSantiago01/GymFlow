package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.meta;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AjusteMetaRequisicao(BigDecimal valorDesejado, LocalDate inicio, LocalDate fim) { }
