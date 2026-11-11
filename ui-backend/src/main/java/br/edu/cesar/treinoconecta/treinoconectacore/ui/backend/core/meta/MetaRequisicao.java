package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.meta;

import java.math.BigDecimal;
import java.time.LocalDate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;

public record MetaRequisicao(AlunoIdentificador aluno, IndicadorFisico indicador, BigDecimal valorDesejado, LocalDate inicio,
        LocalDate fim) { }
