package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.equipamento;

import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.equipamento.EquipamentoProjecao;

public interface IEquipamentoRepositorioJpa extends JpaRepository<EquipamentoJpa, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EquipamentoJpa e where e.identificador = :id")
    Optional<EquipamentoJpa> bloquear(Long id);

    @Query("""
            select count(e) > 0 from EquipamentoJpa e
            where e.patrimonio = :valor and (:ignorado is null or e.identificador <> :ignorado)
            """)
    boolean existePatrimonio(String valor, @Nullable Long ignorado);

    @Query("""
            select count(e) > 0 from EquipamentoJpa e
            where e.serie = :valor and (:ignorado is null or e.identificador <> :ignorado)
            """)
    boolean existeSerie(String valor, @Nullable Long ignorado);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.equipamento.EquipamentoProjecao(
                 e.identificador, e.patrimonio, e.serie, e.nome, e.tipo, e.marca, e.modelo,
                 e.aquisicao, e.area, e.situacao, e.periodicidade, e.ultimaInspecao)
            from EquipamentoJpa e
            where (:termo is null or lower(e.nome) like :termo escape '\\'
                    or lower(e.patrimonio) like :termo escape '\\'
                    or lower(e.serie) like :termo escape '\\'
                    or lower(e.marca) like :termo escape '\\'
                    or lower(e.modelo) like :termo escape '\\')
                and (:situacao is null or e.situacao = :situacao)
                and (:area is null or e.area = :area)
                and (:tipo is null or e.tipo = :tipo)
            """)
    Page<EquipamentoProjecao> pesquisar(@Nullable String termo, @Nullable SituacaoEquipamento situacao, @Nullable AreaEquipamento area, @Nullable TipoEquipamento tipo, Pageable pageable);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.equipamento.EquipamentoProjecao(
                 e.identificador, e.patrimonio, e.serie, e.nome, e.tipo, e.marca, e.modelo,
                 e.aquisicao, e.area, e.situacao, e.periodicidade, e.ultimaInspecao)
            from EquipamentoJpa e
            where e.identificador = :id
            """)
    Optional<EquipamentoProjecao> projetar(Long id);
}
