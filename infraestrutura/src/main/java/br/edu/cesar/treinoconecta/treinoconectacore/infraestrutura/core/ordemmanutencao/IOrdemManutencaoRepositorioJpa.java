package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.ordemmanutencao;

import java.util.Collection;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.ordemmanutencao.OrdemManutencaoProjecao;

public interface IOrdemManutencaoRepositorioJpa extends JpaRepository<OrdemManutencaoJpa, Long> {
    @Override
    @EntityGraph(attributePaths = "problema")
    Optional<OrdemManutencaoJpa> findById(Long identificador);

    boolean existsByProblemaIdentificadorAndSituacaoIn(Long problema, Collection<SituacaoOrdem> situacoes);

    boolean existsByProblemaEquipamentoIdentificadorAndSituacaoIn(Long equipamento, Collection<SituacaoOrdem> situacoes);

    @Query("select o.problema.identificador from OrdemManutencaoJpa o where o.identificador = :id")
    Optional<Long> identificarProblema(Long id);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.ordemmanutencao.OrdemManutencaoProjecao(
                 e.identificador, e.problema.identificador, e.tecnico, e.diagnostico, e.abertura, e.responsavel, e.situacao,
                 e.inicio, e.termino, e.custoEstimado, e.custoReal, e.servicoExecutado, e.justificativa, e.problema.equipamento.identificador, e.problema.titulo)
            from OrdemManutencaoJpa e
            where (:problema is null or e.problema.identificador = :problema)
                and (:situacao is null or e.situacao = :situacao)
            """)
    Page<OrdemManutencaoProjecao> pesquisar(@Nullable Long problema, @Nullable SituacaoOrdem situacao, Pageable pageable);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.ordemmanutencao.OrdemManutencaoProjecao(
                 e.identificador, e.problema.identificador, e.tecnico, e.diagnostico, e.abertura, e.responsavel, e.situacao,
                 e.inicio, e.termino, e.custoEstimado, e.custoReal, e.servicoExecutado, e.justificativa, e.problema.equipamento.identificador, e.problema.titulo)
            from OrdemManutencaoJpa e
            where e.identificador = :id
            """)
    Optional<OrdemManutencaoProjecao> projetar(Long id);
}
