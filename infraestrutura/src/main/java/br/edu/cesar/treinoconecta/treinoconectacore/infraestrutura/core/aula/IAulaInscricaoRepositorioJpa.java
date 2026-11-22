package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aula;

import java.util.List;import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.data.jpa.repository.Query;import org.springframework.data.jpa.repository.Modifying;

public interface IAulaInscricaoRepositorioJpa extends JpaRepository<AulaInscricaoJpa,Long> {
 long countByAulaAndStatus(Long aula,String status); boolean existsByAulaAndAlunoAndStatus(Long aula,Long aluno,String status);
 Optional<AulaInscricaoJpa> findByIdentificadorAndAlunoAndStatus(Long id,Long aluno,String status);
 Optional<AulaInscricaoJpa> findByAulaAndAluno(Long aula,Long aluno);
 List<AulaInscricaoJpa> findByAulaAndStatus(Long aula,String status);
 @Modifying @Query("update AulaInscricaoJpa i set i.status='CANCELADO_PELA_ACADEMIA', i.cancelamento=:instante where i.aula=:aula and i.status='AGENDADO'") int cancelarPorAula(Long aula, java.time.OffsetDateTime instante);
}
