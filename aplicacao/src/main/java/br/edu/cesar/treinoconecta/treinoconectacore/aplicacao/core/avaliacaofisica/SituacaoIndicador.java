package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica;

import java.math.BigDecimal;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.InterpretacaoEvolucao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.TendenciaIndicador;

public record SituacaoIndicador(IndicadorFisico indicador, BigDecimal valor, Optional<TendenciaIndicador> tendencia,
        Optional<InterpretacaoEvolucao> interpretacao) { }
