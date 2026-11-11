package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.planodetreino;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.SituacaoPlano;

public interface IPlanoDeTreinoRepositorioJpa extends JpaRepository<PlanoDeTreinoJpa, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PlanoDeTreinoJpa p where p.identificador = :identificador")
    Optional<PlanoDeTreinoJpa> bloquear(Long identificador);

    @Query("select count(p) > 0 from PlanoDeTreinoJpa p where p.aluno.identificador = :aluno and p.situacao = 'ATIVO'")
    boolean existeAtivoPorAluno(Long aluno);

    @Query("select p from PlanoDeTreinoJpa p where p.aluno.identificador = :aluno and p.situacao = 'ATIVO'")
    Optional<PlanoDeTreinoJpa> buscarAtivoPorAluno(Long aluno);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoProjecao(p.identificador, a.identificador, a.nome, r.identificador, r.nome, p.nome, p.frequenciaSemanal, p.situacao, p.versao, p.criacao, p.encerramento)
              from PlanoDeTreinoJpa p
              join p.aluno a
              join p.personal r
             where (:aluno is null or a.identificador = :aluno)
               and (:situacao is null or p.situacao = :situacao)
               and (:usuario is null or a.usuario = :usuario)
            """)
    Page<PlanoDeTreinoProjecao> pesquisar(@Nullable Long aluno, @Nullable SituacaoPlano situacao, @Nullable String usuario,
            Pageable pageable);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoProjecao(p.identificador, a.identificador, a.nome, r.identificador, r.nome, p.nome, p.frequenciaSemanal, p.situacao, p.versao, p.criacao, p.encerramento)
              from PlanoDeTreinoJpa p
              join p.aluno a
              join p.personal r
             where p.identificador = :identificador
            """)
    Optional<PlanoDeTreinoProjecao> projetar(Long identificador);
}
