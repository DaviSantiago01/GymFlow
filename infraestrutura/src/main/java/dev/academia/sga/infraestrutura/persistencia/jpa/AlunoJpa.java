package dev.academia.sga.infraestrutura.persistencia.jpa;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import dev.academia.sga.aplicacao.administracao.aluno.AlunoRepositorioAplicacao;
import dev.academia.sga.aplicacao.administracao.aluno.AlunoResumo;
import dev.academia.sga.aplicacao.administracao.aluno.AlunoResumoExpandido;
import dev.academia.sga.dominio.administracao.aluno.Aluno;
import dev.academia.sga.dominio.administracao.aluno.AlunoId;
import dev.academia.sga.dominio.administracao.aluno.AlunoRepositorio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "ALUNO")
class AlunoJpa {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID")
	int id;

	@Column(name = "NOME", nullable = false)
	String nome;

	@Column(name = "EMAIL_CONTATO", nullable = false)
	String emailContato;

	@OneToMany(mappedBy = "aluno")
	Set<TreinoJpa> treinos;

	@Override
	public String toString() {
		return nome;
	}
}

interface AlunoJpaRepository extends JpaRepository<AlunoJpa, Integer> {
	List<AlunoResumo> findAlunoResumoByOrderByNome();

	// @formatter:off
	@Query("""
			SELECT a        AS aluno,
			       COUNT(t) AS totalTreinos
			  FROM AlunoJpa   a
		 LEFT JOIN a.treinos  t
		  GROUP BY a
		  ORDER BY a.nome
			""")
	// @formatter:on
	List<AlunoResumoExpandido> pesquisarResumosExpandidos();
}

@Repository
class AlunoRepositorioImpl implements AlunoRepositorio, AlunoRepositorioAplicacao {
	@Autowired
	AlunoJpaRepository repositorio;

	@Autowired
	JpaMapeador mapeador;

	@Override
	public void salvar(Aluno aluno) {
		var alunoJpa = mapeador.map(aluno, AlunoJpa.class);
		repositorio.save(alunoJpa);
	}

	@Override
	public Aluno obter(AlunoId id) {
		var alunoJpa = repositorio.findById(id.getId()).get();
		return mapeador.map(alunoJpa, Aluno.class);
	}

	@Override
	public List<AlunoResumo> pesquisarResumos() {
		return repositorio.findAlunoResumoByOrderByNome();
	}

	@Override
	public List<AlunoResumoExpandido> pesquisarResumosExpandidos() {
		return repositorio.pesquisarResumosExpandidos();
	}
}
