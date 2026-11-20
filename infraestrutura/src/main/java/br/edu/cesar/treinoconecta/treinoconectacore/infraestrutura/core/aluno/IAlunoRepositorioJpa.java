package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.*;

public interface IAlunoRepositorioJpa extends JpaRepository<AlunoJpa,Long> {
    boolean existsByUsuario(String usuario);
    Optional<AlunoJpa> findByUsuario(String usuario);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AlunoJpa a where a.usuario = :usuario")
    Optional<AlunoJpa> bloquear(String usuario);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AlunoJpa a where a.identificador = :identificador")
    Optional<AlunoJpa> bloquearPorIdentificador(Long identificador);

    @Query("select count(a) > 0 from AlunoJpa a where a.cpf = :cpf and (:ignorado is null or a.identificador <> :ignorado)")
    boolean existePorCpf(String cpf, @Nullable Long ignorado);

    @Query("select count(a) > 0 from AlunoJpa a where a.email = :email and (:ignorado is null or a.identificador <> :ignorado)")
    boolean existePorEmail(String email, @Nullable Long ignorado);
}
