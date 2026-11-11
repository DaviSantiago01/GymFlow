package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.ExercicioProjecao;

public interface IExercicioRepositorioJpa extends JpaRepository<ExercicioJpa, Long> {
    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.ExercicioProjecao(e.identificador, e.nome, e.categoria, e.cargaMaxima, e.incrementoCarga)
              from ExercicioJpa e
             where (:nome is null or lower(e.nome) like :nome escape '\\')
            """)
    Page<ExercicioProjecao> pesquisar(@Nullable String nome, Pageable pageable);
}
