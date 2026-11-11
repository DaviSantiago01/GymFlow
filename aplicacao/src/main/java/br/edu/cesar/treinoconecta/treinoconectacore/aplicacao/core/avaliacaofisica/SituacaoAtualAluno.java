package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica;

import java.util.List;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.MetaProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;

public record SituacaoAtualAluno(AlunoIdentificador aluno, Optional<AvaliacaoFisicaProjecao> avaliacao,
        List<SituacaoIndicador> indicadores, List<MetaProjecao> metas) { }
