package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aula;

import java.time.LocalDate;import java.time.LocalTime;import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.data.jpa.repository.Query;import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface IAulaRepositorioJpa extends JpaRepository<AulaJpa,Long> {
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from AulaJpa a where a.identificador=:id") Optional<AulaJpa> bloquear(Long id);
 @Query("select count(a)>0 from AulaJpa a where a.professor=:prof and a.data=:data and a.status='ABERTA' and a.inicio<:fim and a.fim>:inicio and (:ignorar<0 or a.identificador<>:ignorar)") boolean existeChoque(Long prof, LocalDate data, LocalTime inicio, LocalTime fim, Long ignorar);
}
