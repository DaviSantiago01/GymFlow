package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.adesaoplano;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano.AdesaoPlanoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.SituacaoAdesao;

public interface IAdesaoPlanoRepositorioJpa extends JpaRepository<AdesaoPlanoJpa, Long> {
    String PROJECAO = """
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano.AdesaoPlanoProjecao(a.identificador, al.identificador, al.nome, p.identificador, p.nome, p.valor, a.periodicidadeMeses, a.inicio, a.encerramento, a.situacao)
              from AdesaoPlanoJpa a
              join a.aluno al
              join a.plano p
            """;

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AdesaoPlanoJpa a where a.identificador = :identificador")
    Optional<AdesaoPlanoJpa> bloquear(Long identificador);

    @Query("select a from AdesaoPlanoJpa a where a.aluno.identificador = :aluno and a.situacao = 'ATIVA'")
    Optional<AdesaoPlanoJpa> buscarAtivaDoAluno(Long aluno);

    @Query("select a from AdesaoPlanoJpa a where a.situacao = 'ATIVA' order by a.identificador")
    List<AdesaoPlanoJpa> buscarAtivas();

    @Query(value = PROJECAO + """
             where (:aluno is null or al.identificador = :aluno)
               and (:situacao is null or a.situacao = :situacao)
               and (:usuario is null or al.usuario = :usuario)
            """, countQuery = """
            select count(a)
              from AdesaoPlanoJpa a
              join a.aluno al
             where (:aluno is null or al.identificador = :aluno)
               and (:situacao is null or a.situacao = :situacao)
               and (:usuario is null or al.usuario = :usuario)
            """)
    Page<AdesaoPlanoProjecao> pesquisar(@Nullable Long aluno, @Nullable SituacaoAdesao situacao, @Nullable String usuario,
            Pageable pageable);

    @Query(PROJECAO + " where a.identificador = :identificador")
    Optional<AdesaoPlanoProjecao> projetar(Long identificador);

    @Query(PROJECAO + " where al.usuario = :usuario and a.situacao = 'ATIVA'")
    Optional<AdesaoPlanoProjecao> projetarAtivaDoUsuario(String usuario);
}
