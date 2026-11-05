package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.armario;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
public interface IArmarioRepositorioJpa extends JpaRepository<ArmarioJpa,String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from ArmarioJpa a where a.identificador = :codigo")
    Optional<ArmarioJpa> bloquear(String codigo);
}
