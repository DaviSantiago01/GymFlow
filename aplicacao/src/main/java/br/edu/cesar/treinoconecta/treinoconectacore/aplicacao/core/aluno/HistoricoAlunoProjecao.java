package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno;

import java.time.Instant;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.TipoAlteracaoAluno;

/** Registro do histórico de alterações do aluno (F005, RN011). */
public record HistoricoAlunoProjecao(Instant instante, TipoAlteracaoAluno tipo, String descricao, String responsavel) { }
