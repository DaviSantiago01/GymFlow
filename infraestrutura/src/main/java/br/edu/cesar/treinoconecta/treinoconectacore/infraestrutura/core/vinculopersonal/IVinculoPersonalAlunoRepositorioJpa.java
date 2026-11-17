package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.vinculopersonal;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.vinculopersonal.VinculoPersonalAlunoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.SituacaoVinculo;

public interface IVinculoPersonalAlunoRepositorioJpa extends JpaRepository<VinculoPersonalAlunoJpa, Long> {
    String PROJECAO = """
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.vinculopersonal.VinculoPersonalAlunoProjecao(v.identificador, p.identificador, p.nome, a.identificador, a.nome, v.inicio, v.encerramento, v.situacao)
              from VinculoPersonalAlunoJpa v
              join v.profissional p
              join v.aluno a
            """;

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from VinculoPersonalAlunoJpa v where v.identificador = :identificador")
    Optional<VinculoPersonalAlunoJpa> bloquear(Long identificador);

    @Query("""
            select count(v) > 0
              from VinculoPersonalAlunoJpa v
             where v.profissional.identificador = :profissional
               and v.aluno.identificador = :aluno
               and v.situacao = 'ATIVO'
            """)
    boolean existeAtivo(Long profissional, Long aluno);

    @Query("select count(v) > 0 from VinculoPersonalAlunoJpa v where v.profissional.identificador = :profissional and v.situacao = 'ATIVO'")
    boolean existeAtivoDoProfissional(Long profissional);

    @Query(value = PROJECAO + """
             where (:profissional is null or p.identificador = :profissional)
               and (:aluno is null or a.identificador = :aluno)
               and (:situacao is null or v.situacao = :situacao)
            """, countQuery = """
            select count(v)
              from VinculoPersonalAlunoJpa v
             where (:profissional is null or v.profissional.identificador = :profissional)
               and (:aluno is null or v.aluno.identificador = :aluno)
               and (:situacao is null or v.situacao = :situacao)
            """)
    Page<VinculoPersonalAlunoProjecao> pesquisar(@Nullable Long profissional, @Nullable Long aluno,
            @Nullable SituacaoVinculo situacao, Pageable pageable);

    @Query(PROJECAO + " where v.identificador = :identificador")
    Optional<VinculoPersonalAlunoProjecao> projetar(Long identificador);
}
