package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.profissional;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.profissional.ProfissionalProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.SituacaoProfissional;

public interface IProfissionalRepositorioJpa extends JpaRepository<ProfissionalJpa, Long> {
    String FILTROS = """
             where (:busca is null or lower(p.nome) like :busca escape '\\' or p.cpf like :busca escape '\\'
                    or lower(p.cref) like :busca escape '\\')
               and (:professor is null or p.professor = :professor)
               and (:personalTrainer is null or p.personalTrainer = :personalTrainer)
               and (:situacao is null or p.situacao = :situacao)
            """;

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProfissionalJpa p where p.identificador = :identificador")
    Optional<ProfissionalJpa> bloquear(Long identificador);

    @Query("select count(p) > 0 from ProfissionalJpa p where p.cpf = :cpf and (:ignorado is null or p.identificador <> :ignorado)")
    boolean existePorCpf(String cpf, @Nullable Long ignorado);

    @Query("select count(p) > 0 from ProfissionalJpa p where p.cref = :cref and (:ignorado is null or p.identificador <> :ignorado)")
    boolean existePorCref(String cref, @Nullable Long ignorado);

    @Query(value = """
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.profissional.ProfissionalProjecao(p.identificador, p.nome, p.cpf, p.cref, p.contato, p.professor, p.personalTrainer, p.situacao,
                   (select count(v) from VinculoPersonalAlunoJpa v where v.profissional = p and v.situacao = 'ATIVO'))
              from ProfissionalJpa p
            """ + FILTROS, countQuery = "select count(p) from ProfissionalJpa p " + FILTROS)
    Page<ProfissionalProjecao> pesquisar(@Nullable String busca, @Nullable Boolean professor,
            @Nullable Boolean personalTrainer, @Nullable SituacaoProfissional situacao, Pageable pageable);

    @Query("select p from ProfissionalJpa p where p.professor = true and p.situacao = :situacao order by p.nome")
    List<ProfissionalJpa> professores(SituacaoProfissional situacao);
}
