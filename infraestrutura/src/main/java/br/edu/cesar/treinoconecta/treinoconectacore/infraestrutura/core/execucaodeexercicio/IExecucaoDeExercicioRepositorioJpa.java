package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.execucaodeexercicio;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExecucaoDeExercicioProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExercicioExecutado;

public interface IExecucaoDeExercicioRepositorioJpa extends JpaRepository<ExecucaoDeExercicioJpa, Long> {
    @Query("""
            select e from ExecucaoDeExercicioJpa e
             where e.aluno.identificador = :aluno and e.exercicio.identificador = :exercicio
             order by e.data, e.identificador
            """)
    List<ExecucaoDeExercicioJpa> buscarPorAlunoEExercicio(Long aluno, Long exercicio);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExecucaoDeExercicioProjecao(e.identificador, a.identificador, a.nome, p.identificador, p.nome, e.dia, x.identificador, x.nome, e.data, e.series, e.repeticoes, e.carga)
              from ExecucaoDeExercicioJpa e
              join e.aluno a
              join e.plano p
              join e.exercicio x
             where (:aluno is null or a.identificador = :aluno)
               and (:exercicio is null or x.identificador = :exercicio)
               and (:usuario is null or a.usuario = :usuario)
            """)
    Page<ExecucaoDeExercicioProjecao> pesquisar(@Nullable Long aluno, @Nullable Long exercicio, @Nullable String usuario,
            Pageable pageable);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExecucaoDeExercicioProjecao(e.identificador, a.identificador, a.nome, p.identificador, p.nome, e.dia, x.identificador, x.nome, e.data, e.series, e.repeticoes, e.carga)
              from ExecucaoDeExercicioJpa e
              join e.aluno a
              join e.plano p
              join e.exercicio x
             where e.identificador = :identificador
            """)
    Optional<ExecucaoDeExercicioProjecao> projetar(Long identificador);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExecucaoDeExercicioProjecao(e.identificador, a.identificador, a.nome, p.identificador, p.nome, e.dia, x.identificador, x.nome, e.data, e.series, e.repeticoes, e.carga)
              from ExecucaoDeExercicioJpa e
              join e.aluno a
              join e.plano p
              join e.exercicio x
             where a.identificador = :aluno and x.identificador = :exercicio
             order by e.data, e.identificador
            """)
    List<ExecucaoDeExercicioProjecao> listarCronologicamente(Long aluno, Long exercicio);

    @Query("""
            select distinct new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExercicioExecutado(x.identificador, x.nome)
              from ExecucaoDeExercicioJpa e
              join e.exercicio x
             where e.aluno.identificador = :aluno
             order by x.nome
            """)
    List<ExercicioExecutado> exerciciosExecutados(Long aluno);
}
