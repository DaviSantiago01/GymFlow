package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.metaacademia;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.metaacademia.MetaAcademiaProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.SituacaoMetaAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.IndicadorAcademia;
import jakarta.persistence.LockModeType;

public interface IMetaAcademiaRepositorioJpa extends JpaRepository<MetaAcademiaJpa, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MetaAcademiaJpa m where m.identificador = :identificador")
    Optional<MetaAcademiaJpa> bloquear(Long identificador);

    @Query("""
            select count(m) > 0
              from MetaAcademiaJpa m
             where m.indicador = :indicador
               and m.situacao = 'ATIVA'
               and m.inicio <= :fim
               and m.fim >= :inicio
               and (:ignorada is null or m.identificador <> :ignorada)
            """)
    boolean existeAtivaSobreposta(IndicadorAcademia indicador, LocalDate inicio, LocalDate fim, @Nullable Long ignorada);

    @Query("""
            select m
              from MetaAcademiaJpa m
             where m.situacao <> 'SUBSTITUIDA'
               and m.inicio <= :fim
               and m.fim >= :inicio
             order by m.inicio, m.indicador
            """)
    List<MetaAcademiaJpa> buscarVigentes(LocalDate inicio, LocalDate fim);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.metaacademia.MetaAcademiaProjecao(
                   m.identificador, m.indicador, m.valorDesejado, m.inicio, m.fim, m.situacao, m.criacao, m.responsavel)
              from MetaAcademiaJpa m
             where (:situacao is null or m.situacao = :situacao)
            """)
    Page<MetaAcademiaProjecao> pesquisar(@Nullable SituacaoMetaAcademia situacao, Pageable pageable);
}
