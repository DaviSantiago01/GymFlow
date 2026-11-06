package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.grupotreino;

import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
public interface IGrupoTreinoRepositorioJpa extends JpaRepository<GrupoTreinoJpa,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select g from GrupoTreinoJpa g where g.identificador=:id") Optional<GrupoTreinoJpa> bloquear(Long id);
}
