package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi;

import java.time.Instant;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.TipoAlteracaoAluno;

/** Histórico de alterações do aluno (F005, RN011). Os registros nunca são alterados. */
public interface IHistoricoAlunoRepositorio {
    void registrar(AlunoIdentificador aluno, TipoAlteracaoAluno tipo, String descricao, String responsavel, Instant instante);
}
