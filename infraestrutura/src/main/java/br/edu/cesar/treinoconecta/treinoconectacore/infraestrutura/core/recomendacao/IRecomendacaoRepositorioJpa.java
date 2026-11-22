package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.recomendacao;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.RecomendacaoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.CategoriaRecomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.SituacaoRecomendacao;
import jakarta.persistence.LockModeType;

public interface IRecomendacaoRepositorioJpa extends JpaRepository<RecomendacaoJpa, Long> {
    String PROJECAO = """
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.RecomendacaoProjecao(
                   r.identificador, a.identificador, a.nome, r.categoria, r.criterio, r.titulo, r.descricao,
                   r.justificativa, r.situacao, r.geracao, r.validade, r.resposta, r.respondente, r.motivo, r.ajuste,
                   p.identificador, p.nome, pt.identificador, pt.nome, pt.versao, ea.nome, es.nome, r.aula, r.plano,
                   pg.identificador, pg.versao, r.ciencia)
              from RecomendacaoJpa r
              join r.aluno a
              left join r.personal p
              left join r.planoTreino pt
              left join r.exercicioAtual ea
              left join r.exercicioSugerido es
              left join r.planoGerado pg
            """;

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RecomendacaoJpa r where r.identificador = :identificador")
    Optional<RecomendacaoJpa> bloquear(Long identificador);

    @Query("select r from RecomendacaoJpa r where r.aluno.identificador = :aluno order by r.geracao")
    List<RecomendacaoJpa> buscarPorAluno(Long aluno);

    @Query("""
            select r.identificador from RecomendacaoJpa r
             where (r.situacao = 'PENDENTE' or r.situacao = 'AGUARDANDO_APROVACAO') and r.validade < :hoje
             order by r.validade
            """)
    List<Long> buscarVencidas(LocalDate hoje, Pageable pagina);

    /**
     * O aluno vê apenas as próprias recomendações, sem as sugestões de treino que o personal ainda não aprovou (RN011).
     * O personal vê as sugestões enviadas a ele e o histórico dos alunos que acompanha; com {@code sugestoes}, apenas
     * as sugestões de treino enviadas a ele.
     */
    @Query(PROJECAO + """
             where (:aluno is null or a.identificador = :aluno)
               and (:situacao is null or r.situacao = :situacao)
               and (:categoria is null or r.categoria = :categoria)
               and (:desde is null or r.geracao >= :desde)
               and (:usuarioAluno is null or a.usuario = :usuarioAluno)
               and (:usuarioPersonal is null or p.usuario = :usuarioPersonal
                    or (:sugestoes = false and exists (select 1 from PlanoDeTreinoJpa x
                         where x.aluno = a and x.personal.usuario = :usuarioPersonal)))
               and (:sugestoes = false or (r.categoria = 'AJUSTES_TREINO' and p.usuario = :usuarioPersonal))
               and (:usuarioAluno is null or (r.situacao <> 'AGUARDANDO_APROVACAO' and r.situacao <> 'REJEITADA'))
            """)
    Page<RecomendacaoProjecao> pesquisar(@Nullable Long aluno, @Nullable SituacaoRecomendacao situacao,
            @Nullable CategoriaRecomendacao categoria, @Nullable Instant desde, @Nullable String usuarioAluno,
            @Nullable String usuarioPersonal, boolean sugestoes, Pageable pageable);

    @Query(PROJECAO + " where r.identificador = :identificador")
    Optional<RecomendacaoProjecao> projetar(Long identificador);
}
