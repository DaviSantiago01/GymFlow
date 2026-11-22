package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.desafio;
import java.util.Optional;import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.data.jpa.repository.Query;import org.springframework.data.jpa.repository.Lock;import jakarta.persistence.LockModeType;
public interface IDesafioRepositorioJpa extends JpaRepository<DesafioJpa,Long>{@Lock(LockModeType.PESSIMISTIC_WRITE)@Query("select d from DesafioJpa d where d.identificador=:id")Optional<DesafioJpa> bloquear(Long id);}
