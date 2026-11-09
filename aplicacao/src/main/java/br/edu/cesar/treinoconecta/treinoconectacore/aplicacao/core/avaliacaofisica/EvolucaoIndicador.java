package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica;

import java.util.List;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.InterpretacaoEvolucao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.PontoIndicador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.TendenciaIndicador;

public record EvolucaoIndicador(AlunoIdentificador aluno, IndicadorFisico indicador, List<IndicadorFisico> indicadores,
        List<PontoIndicador> pontos, Optional<TendenciaIndicador> tendencia, Optional<InterpretacaoEvolucao> interpretacao) { }
