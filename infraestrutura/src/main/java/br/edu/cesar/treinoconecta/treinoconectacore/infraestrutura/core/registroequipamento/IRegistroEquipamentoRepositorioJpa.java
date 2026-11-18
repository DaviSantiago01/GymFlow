package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.registroequipamento;

import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroequipamento.RegistroEquipamentoProjecao;

public interface IRegistroEquipamentoRepositorioJpa extends JpaRepository<RegistroEquipamentoJpa, Long> {
    @Override
    @EntityGraph(attributePaths = "equipamento")
    Optional<RegistroEquipamentoJpa> findById(Long identificador);

    boolean existsByEquipamentoIdentificador(Long equipamento);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroequipamento.RegistroEquipamentoProjecao(
                 e.identificador, e.equipamento.identificador, e.data, e.responsavel, e.areaAnterior, e.areaNova, e.situacaoAnterior,
                 e.situacaoNova, e.resultado, e.observacao, e.equipamento.nome)
            from RegistroEquipamentoJpa e
            where (:equipamento is null or e.equipamento.identificador = :equipamento)
            """)
    Page<RegistroEquipamentoProjecao> pesquisar(@Nullable Long equipamento, Pageable pageable);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroequipamento.RegistroEquipamentoProjecao(
                 e.identificador, e.equipamento.identificador, e.data, e.responsavel, e.areaAnterior, e.areaNova, e.situacaoAnterior,
                 e.situacaoNova, e.resultado, e.observacao, e.equipamento.nome)
            from RegistroEquipamentoJpa e
            where e.identificador = :id
            """)
    Optional<RegistroEquipamentoProjecao> projetar(Long id);
}
