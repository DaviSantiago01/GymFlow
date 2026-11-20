package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.mensalidade;

import java.time.LocalDate;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeProjecao;

public interface IMensalidadeRepositorioJpa extends JpaRepository<MensalidadeJpa, Long> {
    String PROJECAO = """
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeProjecao(m.identificador, ad.identificador, al.identificador, al.nome, p.nome, m.periodoInicio, m.periodoFim, m.vencimento, m.valor, m.situacao)
              from MensalidadeJpa m
              join m.adesao ad
              join ad.plano p
              join m.aluno al
            """;
    String FILTROS = """
             where (:aluno is null or al.identificador = :aluno)
               and (:adesao is null or ad.identificador = :adesao)
               and (:usuario is null or al.usuario = :usuario)
               and (:filtro is null
                    or (:filtro = 'PAGA' and m.situacao = 'PAGA')
                    or (:filtro = 'ABERTA' and m.situacao = 'ABERTA' and m.vencimento >= :hoje)
                    or (:filtro = 'ATRASADA' and m.situacao = 'ABERTA' and m.vencimento < :hoje))
            """;

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MensalidadeJpa m where m.identificador = :identificador")
    Optional<MensalidadeJpa> bloquear(Long identificador);

    @Query("select count(m) > 0 from MensalidadeJpa m where m.adesao.identificador = :adesao and m.periodoInicio = :inicio")
    boolean existePorAdesaoEPeriodo(Long adesao, LocalDate inicio);

    @Query("select count(m) > 0 from MensalidadeJpa m where m.aluno.identificador = :aluno and m.situacao = 'ABERTA' and m.vencimento < :hoje")
    boolean existeAtrasadaDoAluno(Long aluno, LocalDate hoje);

    @Query(value = PROJECAO + FILTROS, countQuery = """
            select count(m)
              from MensalidadeJpa m
              join m.adesao ad
              join m.aluno al
            """ + FILTROS)
    Page<MensalidadeProjecao> pesquisar(@Nullable Long aluno, @Nullable Long adesao, @Nullable String usuario,
            @Nullable String filtro, LocalDate hoje, Pageable pageable);

    @Query(PROJECAO + " where m.identificador = :identificador")
    Optional<MensalidadeProjecao> projetar(Long identificador);
}
