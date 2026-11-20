package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.spi;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoGestaoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.HistoricoAlunoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;

/** Consultas da administração de alunos (F005). */
public interface IAlunoGestaoRepositorio {
    /** Procura o trecho no nome, no usuário, no CPF ou no e-mail; a mensalidade atrasada é avaliada na data informada. */
    List<AlunoGestaoProjecao> pesquisar(Optional<String> busca, LocalDate hoje);
    Optional<AlunoGestaoProjecao> projetar(AlunoIdentificador aluno, LocalDate hoje);
    List<HistoricoAlunoProjecao> historico(AlunoIdentificador aluno);
}
