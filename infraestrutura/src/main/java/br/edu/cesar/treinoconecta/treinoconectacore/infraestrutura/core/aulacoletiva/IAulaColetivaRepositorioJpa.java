package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aulacoletiva;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

public interface IAulaColetivaRepositorioJpa extends JpaRepository<AulaColetivaJpa, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AulaColetivaJpa a where a.identificador = :identificador")
    Optional<AulaColetivaJpa> bloquear(Long identificador);

    @Query("select a from AulaColetivaJpa a where a.ativa = true order by a.dia, a.inicio")
    List<AulaColetivaJpa> buscarAtivas();

    @Query("select distinct a from AulaColetivaJpa a join a.inscricoes i where i.aluno = :aluno")
    List<AulaColetivaJpa> buscarInscricoesDoAluno(Long aluno);
}
