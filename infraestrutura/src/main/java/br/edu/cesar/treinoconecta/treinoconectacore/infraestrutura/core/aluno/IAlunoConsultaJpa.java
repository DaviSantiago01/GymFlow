package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoProjecao;

public interface IAlunoConsultaJpa extends JpaRepository<AlunoJpa, Long> {
    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoProjecao(a.identificador, a.usuario, a.nome)
              from AlunoJpa a
             where (:nome is null or lower(a.nome) like :nome escape '\\')
            """)
    Page<AlunoProjecao> pesquisar(@Nullable String nome, Pageable pageable);
}
