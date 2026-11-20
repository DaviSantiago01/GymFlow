package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.HistoricoAlunoProjecao;

public interface IHistoricoAlunoRepositorioJpa extends JpaRepository<HistoricoAlunoJpa, Long> {
    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.HistoricoAlunoProjecao(h.instante, h.tipo, h.descricao, h.responsavel)
              from HistoricoAlunoJpa h
             where h.aluno.identificador = :aluno
             order by h.instante desc, h.identificador desc
            """)
    List<HistoricoAlunoProjecao> doAluno(Long aluno);
}
