package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.plano;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.PlanoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.SituacaoPlano;

public interface IPlanoRepositorioJpa extends JpaRepository<PlanoJpa, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PlanoJpa p where p.identificador = :identificador")
    Optional<PlanoJpa> bloquear(Long identificador);

    @Query("""
            select count(p) > 0
              from PlanoJpa p
             where p.situacao = 'ATIVO'
               and lower(p.nome) = lower(:nome)
               and p.duracaoMeses = :duracaoMeses
               and (:ignorado is null or p.identificador <> :ignorado)
            """)
    boolean existeAtivoComNomeEDuracao(String nome, int duracaoMeses, @Nullable Long ignorado);

    @Query(value = """
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.PlanoProjecao(p.identificador, p.nome, p.valor, p.duracaoMeses, p.situacao,
                   (select count(a) from AdesaoPlanoJpa a where a.plano = p and a.situacao = 'ATIVA'))
              from PlanoJpa p
             where (:nome is null or lower(p.nome) like :nome escape '\\')
               and (:situacao is null or p.situacao = :situacao)
            """, countQuery = """
            select count(p)
              from PlanoJpa p
             where (:nome is null or lower(p.nome) like :nome escape '\\')
               and (:situacao is null or p.situacao = :situacao)
            """)
    Page<PlanoProjecao> pesquisar(@Nullable String nome, @Nullable SituacaoPlano situacao, Pageable pageable);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.PlanoProjecao(p.identificador, p.nome, p.valor, p.duracaoMeses, p.situacao,
                   (select count(a) from AdesaoPlanoJpa a where a.plano = p and a.situacao = 'ATIVA'))
              from PlanoJpa p
             where p.identificador = :identificador
            """)
    Optional<PlanoProjecao> projetar(Long identificador);
}
