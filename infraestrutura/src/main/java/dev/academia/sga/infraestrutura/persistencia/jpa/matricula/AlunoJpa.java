package dev.academia.sga.infraestrutura.persistencia.jpa.matricula;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.academia.sga.aplicacao.matricula.aluno.AlunoRepositorioAplicacao;
import dev.academia.sga.aplicacao.matricula.aluno.AlunoResumo;
import dev.academia.sga.dominio.compartilhado.AlunoId;
import dev.academia.sga.dominio.matricula.aluno.Aluno;
import dev.academia.sga.dominio.matricula.aluno.AlunoRepositorio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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

	@Override
	public String toString() {
		return nome;
	}
}

interface AlunoJpaRepository extends JpaRepository<AlunoJpa, Integer> {
	List<AlunoResumo> findAlunoResumoByOrderByNome();
}

@Repository
class AlunoRepositorioImpl implements AlunoRepositorio, AlunoRepositorioAplicacao {
	@Autowired
	AlunoJpaRepository repositorio;

	@Autowired
	MatriculaJpaMapeador mapeador;

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
}
