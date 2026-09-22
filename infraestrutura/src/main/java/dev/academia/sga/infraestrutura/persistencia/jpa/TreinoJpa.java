package dev.academia.sga.infraestrutura.persistencia.jpa;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import dev.academia.sga.aplicacao.treinamento.treino.TreinoRepositorioAplicacao;
import dev.academia.sga.aplicacao.treinamento.treino.TreinoResumo;
import dev.academia.sga.aplicacao.treinamento.treino.TreinoResumoExpandido;
import dev.academia.sga.dominio.treinamento.treino.Treino;
import dev.academia.sga.dominio.treinamento.treino.TreinoId;
import dev.academia.sga.dominio.treinamento.treino.TreinoRepositorio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;

@Entity
@Table(name = "TREINO")
class TreinoJpa {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID")
	int id;

	@Column(name = "NOME", nullable = false)
	String nome;

	@Column(name = "OBJETIVO")
	String objetivo;

	@ManyToOne
	@JoinColumn(name = "ALUNO_ID")
	AlunoJpa aluno;

	@ManyToOne
	@JoinColumn(name = "INSTRUTOR_ID")
	InstrutorJpa instrutor;

	@ManyToMany
	@JoinTable(name = "TREINO_EXERCICIO", joinColumns = @JoinColumn(name = "TREINO_ID"), inverseJoinColumns = @JoinColumn(name = "EXERCICIO_ID"))
	@OrderColumn(name = "EXERCICIO_ORDEM")
	List<ExercicioJpa> exercicios;

	@Override
	public String toString() {
		return nome;
	}
}

interface TreinoJpaRepository extends JpaRepository<TreinoJpa, Integer> {
	List<TreinoResumo> findTreinoResumoByOrderByNome();

	// @formatter:off
	@Query("""
			SELECT t                  AS treino,
			       a                  AS aluno,
			       i                  AS instrutor,
			       SIZE(t.exercicios) AS totalExercicios
			  FROM TreinoJpa     t
			  JOIN t.aluno       a
			  JOIN t.instrutor   i
			 ORDER BY a.nome,
			          t.nome
			""")
	// @formatter:on
	List<TreinoResumoExpandido> pesquisarResumosExpandidos();
}

@Repository
class TreinoRepositorioImpl implements TreinoRepositorio, TreinoRepositorioAplicacao {
	@Autowired
	TreinoJpaRepository repositorio;

	@Autowired
	JpaMapeador mapeador;

	@Override
	public void salvar(Treino treino) {
		var treinoJpa = mapeador.map(treino, TreinoJpa.class);
		repositorio.save(treinoJpa);
	}

	@Transactional
	@Override
	public Treino obter(TreinoId id) {
		var treinoJpa = repositorio.findById(id.getId()).get();
		return mapeador.map(treinoJpa, Treino.class);
	}

	@Override
	public List<TreinoResumo> pesquisarResumos() {
		return repositorio.findTreinoResumoByOrderByNome();
	}

	@Override
	public List<TreinoResumoExpandido> pesquisarResumosExpandidos() {
		return repositorio.pesquisarResumosExpandidos();
	}
}
