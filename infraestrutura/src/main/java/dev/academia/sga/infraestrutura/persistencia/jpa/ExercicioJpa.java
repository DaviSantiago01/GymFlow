package dev.academia.sga.infraestrutura.persistencia.jpa;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.academia.sga.aplicacao.treinamento.exercicio.ExercicioRepositorioAplicacao;
import dev.academia.sga.aplicacao.treinamento.exercicio.ExercicioResumo;
import dev.academia.sga.dominio.treinamento.exercicio.Exercicio;
import dev.academia.sga.dominio.treinamento.exercicio.ExercicioId;
import dev.academia.sga.dominio.treinamento.exercicio.ExercicioRepositorio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "EXERCICIO")
class ExercicioJpa {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID")
	int id;

	@Column(name = "NOME", nullable = false)
	String nome;

	@Column(name = "GRUPO_MUSCULAR", nullable = false)
	String grupoMuscular;

	@ManyToMany(mappedBy = "exercicios")
	Set<TreinoJpa> treinos;

	@Override
	public String toString() {
		return nome;
	}
}

interface ExercicioJpaRepository extends JpaRepository<ExercicioJpa, Integer> {
	List<ExercicioResumo> findExercicioResumoByOrderByNome();

	// @formatter:off
	@Query("""
			SELECT e
			  FROM TreinoJpa      t
			  JOIN t.exercicios   e
			 WHERE t.id = :treino
			 ORDER BY INDEX(e)
			""")
	// @formatter:on
	List<ExercicioResumo> pesquisarResumosPorTreino(@Param("treino") int treino);
}

@Repository
class ExercicioRepositorioImpl implements ExercicioRepositorio, ExercicioRepositorioAplicacao {
	@Autowired
	ExercicioJpaRepository repositorio;

	@Autowired
	JpaMapeador mapeador;

	@Override
	public void salvar(Exercicio exercicio) {
		var exercicioJpa = mapeador.map(exercicio, ExercicioJpa.class);
		repositorio.save(exercicioJpa);
	}

	@Override
	public Exercicio obter(ExercicioId id) {
		var exercicioJpa = repositorio.findById(id.getId()).get();
		return mapeador.map(exercicioJpa, Exercicio.class);
	}

	@Override
	public List<ExercicioResumo> pesquisarResumos() {
		return repositorio.findExercicioResumoByOrderByNome();
	}

	@Override
	public List<ExercicioResumo> pesquisarResumosPorTreino(int treino) {
		return repositorio.pesquisarResumosPorTreino(treino);
	}
}
