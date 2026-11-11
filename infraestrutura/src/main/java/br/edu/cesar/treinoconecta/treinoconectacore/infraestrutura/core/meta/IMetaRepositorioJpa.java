package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.meta;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.MetaProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.SituacaoMeta;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaofisica.AvaliacaoFisicaJpa;

public interface IMetaRepositorioJpa extends JpaRepository<MetaJpa, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MetaJpa m where m.identificador = :identificador")
    Optional<MetaJpa> bloquear(Long identificador);

    @Query("""
            select count(m) > 0
              from MetaJpa m
             where m.aluno.identificador = :aluno
               and m.indicador = :indicador
               and m.situacao = 'ATIVA'
               and m.inicio <= :fim
               and m.fim >= :inicio
               and (:ignorada is null or m.identificador <> :ignorada)
            """)
    boolean existeAtivaNoPeriodo(Long aluno, IndicadorFisico indicador, LocalDate inicio, LocalDate fim, @Nullable Long ignorada);

    @Query("select m from MetaJpa m where m.aluno.identificador = :aluno and m.situacao = 'ATIVA'")
    List<MetaJpa> buscarAtivasPorAluno(Long aluno);

    @Query("select count(m) > 0 from MetaJpa m where m.avaliacaoReferencia.identificador = :avaliacao")
    boolean existePorAvaliacaoReferencia(Long avaliacao);

    @Modifying(flushAutomatically=true, clearAutomatically=true)
    @Query("update MetaJpa m set m.avaliacaoReferencia = :nova where m.avaliacaoReferencia = :antiga")
    int transferirReferencia(AvaliacaoFisicaJpa antiga, AvaliacaoFisicaJpa nova);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.MetaProjecao(m.identificador, a.identificador, a.nome, m.indicador, m.valorInicial, m.valorDesejado, m.inicio, m.fim, m.situacao, m.dataAtingida)
              from MetaJpa m
              join m.aluno a
             where (:aluno is null or a.identificador = :aluno)
               and (:situacao is null or m.situacao = :situacao)
               and (:usuario is null or a.usuario = :usuario)
            """)
    Page<MetaProjecao> pesquisar(@Nullable Long aluno, @Nullable SituacaoMeta situacao, @Nullable String usuario, Pageable pageable);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.MetaProjecao(m.identificador, a.identificador, a.nome, m.indicador, m.valorInicial, m.valorDesejado, m.inicio, m.fim, m.situacao, m.dataAtingida)
              from MetaJpa m
              join m.aluno a
             where m.identificador = :identificador
            """)
    Optional<MetaProjecao> projetar(Long identificador);
}
