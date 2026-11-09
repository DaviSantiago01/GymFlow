package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta;

import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;

public record MetaFormulario(List<AlunoProjecao> alunos, List<IndicadorFisico> indicadores) { }
