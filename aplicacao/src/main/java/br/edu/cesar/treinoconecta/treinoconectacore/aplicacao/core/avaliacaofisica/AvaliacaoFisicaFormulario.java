package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica;

import java.math.BigDecimal;
import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;

public record AvaliacaoFisicaFormulario(List<AlunoProjecao> alunos, List<IndicadorFisico> medidas, BigDecimal pesoMinimo,
        BigDecimal pesoMaximo, BigDecimal alturaMinima, BigDecimal alturaMaxima, BigDecimal medidaMinima,
        BigDecimal medidaMaxima) { }
