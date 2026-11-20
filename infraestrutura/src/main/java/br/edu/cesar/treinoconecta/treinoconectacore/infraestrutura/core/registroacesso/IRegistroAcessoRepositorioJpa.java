package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.registroacesso;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso.RegistroAcessoProjecao;

public interface IRegistroAcessoRepositorioJpa extends JpaRepository<RegistroAcessoJpa, Long> {
    String PROJECAO = """
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso.RegistroAcessoProjecao(
                   r.identificador, a.identificador, a.nome, a.usuario, r.entrada, r.saida, r.verificar)
              from RegistroAcessoJpa r join r.aluno a
            """;
    String FILTROS = """
             where (:aluno is null or a.identificador = :aluno)
               and (:inicio is null or r.entrada >= :inicio)
               and (:fim is null or r.entrada < :fim)
            """;

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RegistroAcessoJpa r where r.aluno.identificador = :aluno and r.saida is null")
    Optional<RegistroAcessoJpa> bloquearAbertoDoAluno(Long aluno);

    @Query("select count(r) from RegistroAcessoJpa r where r.saida is null and r.aluno.academia.identificador = :academia")
    long contarPresentes(Long academia);

    @Query(PROJECAO + " where r.saida is null order by r.entrada")
    List<RegistroAcessoProjecao> presentes();

    @Query(PROJECAO + " where a.identificador = :aluno and r.entrada >= :inicio and r.entrada < :fim order by r.entrada desc")
    List<RegistroAcessoProjecao> doAluno(Long aluno, Instant inicio, Instant fim);

    @Query(value = PROJECAO + FILTROS, countQuery = "select count(r) from RegistroAcessoJpa r join r.aluno a " + FILTROS)
    Page<RegistroAcessoProjecao> pesquisar(@Nullable Long aluno, @Nullable Instant inicio, @Nullable Instant fim,
            Pageable pageable);
}
