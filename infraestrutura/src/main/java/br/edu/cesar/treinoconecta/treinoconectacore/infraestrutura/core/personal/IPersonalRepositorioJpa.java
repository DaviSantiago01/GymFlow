package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPersonalRepositorioJpa extends JpaRepository<PersonalJpa, Long> {
    Optional<PersonalJpa> findByUsuario(String usuario);
}
