package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaofisica;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.AvaliacaoFisicaProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.SituacaoAvaliacao;

public interface IAvaliacaoFisicaRepositorioJpa extends JpaRepository<AvaliacaoFisicaJpa, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AvaliacaoFisicaJpa a where a.identificador = :identificador")
    Optional<AvaliacaoFisicaJpa> bloquear(Long identificador);

    @Query("select a from AvaliacaoFisicaJpa a where a.aluno.identificador = :aluno and a.situacao = 'VALIDA' order by a.data")
    List<AvaliacaoFisicaJpa> buscarValidasPorAluno(Long aluno);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.AvaliacaoFisicaProjecao(f.identificador, a.identificador, a.nome, p.nome, f.data, f.peso, f.altura, f.imc, f.situacao, f.versao)
              from AvaliacaoFisicaJpa f
              join f.aluno a
              join f.personal p
             where (:aluno is null or a.identificador = :aluno)
               and (:situacao is null or f.situacao = :situacao)
               and (:usuario is null or a.usuario = :usuario)
            """)
    Page<AvaliacaoFisicaProjecao> pesquisar(@Nullable Long aluno, @Nullable SituacaoAvaliacao situacao, @Nullable String usuario,
            Pageable pageable);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.AvaliacaoFisicaProjecao(f.identificador, a.identificador, a.nome, p.nome, f.data, f.peso, f.altura, f.imc, f.situacao, f.versao)
              from AvaliacaoFisicaJpa f
              join f.aluno a
              join f.personal p
             where f.identificador = :identificador
            """)
    Optional<AvaliacaoFisicaProjecao> projetar(Long identificador);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.AvaliacaoFisicaProjecao(f.identificador, a.identificador, a.nome, p.nome, f.data, f.peso, f.altura, f.imc, f.situacao, f.versao)
              from AvaliacaoFisicaJpa f
              join f.aluno a
              join f.personal p
             where a.identificador = :aluno and f.situacao = 'VALIDA'
             order by f.data
            """)
    List<AvaliacaoFisicaProjecao> listarValidas(Long aluno);
}
