package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.desafio;
import java.util.Optional;import org.springframework.data.jpa.repository.JpaRepository;
public interface IDesafioInscricaoRepositorioJpa extends JpaRepository<DesafioInscricaoJpa,Long>{boolean existsByDesafioAndAluno(Long desafio,Long aluno);Optional<DesafioInscricaoJpa> findByDesafioAndAluno(Long desafio,Long aluno);}
