package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
public interface IAlunoRepositorioJpa extends JpaRepository<AlunoJpa,Long> {
    boolean existsByUsuario(String usuario);
    Optional<AlunoJpa> findByUsuario(String usuario);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AlunoJpa a where a.usuario = :usuario")
    Optional<AlunoJpa> bloquear(String usuario);
}
