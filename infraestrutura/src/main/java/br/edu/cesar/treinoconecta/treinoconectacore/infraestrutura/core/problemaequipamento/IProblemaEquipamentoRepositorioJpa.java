package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.problemaequipamento;

import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.problemaequipamento.ProblemaEquipamentoProjecao;

public interface IProblemaEquipamentoRepositorioJpa extends JpaRepository<ProblemaEquipamentoJpa, Long> {
    @Override
    @EntityGraph(attributePaths = "equipamento")
    Optional<ProblemaEquipamentoJpa> findById(Long identificador);

    boolean existsByEquipamentoIdentificador(Long equipamento);

    boolean existsByEquipamentoIdentificadorAndSituacao(Long equipamento, SituacaoProblema situacao);

    @Query("""
            select count(p) > 0 from ProblemaEquipamentoJpa p
            where p.equipamento.identificador = :equipamento
                and p.situacao = :situacao and (p.risco = true or p.gravidade = :critica)
            """)
    boolean existeRisco(Long equipamento, SituacaoProblema situacao, GravidadeProblema critica);

    @Query("select p.equipamento.identificador from ProblemaEquipamentoJpa p where p.identificador = :id")
    Optional<Long> identificarEquipamento(Long id);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.problemaequipamento.ProblemaEquipamentoProjecao(
                 e.identificador, e.equipamento.identificador, e.titulo, e.relato, e.gravidade, e.risco, e.evidencia,
                 e.dataRelato, e.responsavel, e.situacao, e.dataEncerramento, e.desfecho, e.equipamento.nome)
            from ProblemaEquipamentoJpa e
            where (:equipamento is null or e.equipamento.identificador = :equipamento)
                and (:situacao is null or e.situacao = :situacao)
                and (:gravidade is null or e.gravidade = :gravidade)
            """)
    Page<ProblemaEquipamentoProjecao> pesquisar(@Nullable Long equipamento, @Nullable SituacaoProblema situacao, @Nullable GravidadeProblema gravidade, Pageable pageable);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.problemaequipamento.ProblemaEquipamentoProjecao(
                 e.identificador, e.equipamento.identificador, e.titulo, e.relato, e.gravidade, e.risco, e.evidencia,
                 e.dataRelato, e.responsavel, e.situacao, e.dataEncerramento, e.desfecho, e.equipamento.nome)
            from ProblemaEquipamentoJpa e
            where e.identificador = :id
            """)
    Optional<ProblemaEquipamentoProjecao> projetar(Long id);
}
