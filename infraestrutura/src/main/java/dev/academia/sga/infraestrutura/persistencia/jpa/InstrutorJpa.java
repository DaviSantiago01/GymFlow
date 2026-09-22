package dev.academia.sga.infraestrutura.persistencia.jpa;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.academia.sga.aplicacao.administracao.instrutor.InstrutorRepositorioAplicacao;
import dev.academia.sga.aplicacao.administracao.instrutor.InstrutorResumo;
import dev.academia.sga.dominio.administracao.instrutor.Instrutor;
import dev.academia.sga.dominio.administracao.instrutor.InstrutorId;
import dev.academia.sga.dominio.administracao.instrutor.InstrutorRepositorio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "INSTRUTOR")
class InstrutorJpa {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID")
	int id;

	@Column(name = "NOME", nullable = false)
	String nome;

	@Column(name = "ESPECIALIDADE")
	String especialidade;

	@OneToMany(mappedBy = "instrutor")
	Set<TreinoJpa> treinos;

	@Override
	public String toString() {
		return nome;
	}
}

interface InstrutorJpaRepository extends JpaRepository<InstrutorJpa, Integer> {
	List<InstrutorResumo> findInstrutorResumoByOrderByNome();
}

@Repository
class InstrutorRepositorioImpl implements InstrutorRepositorio, InstrutorRepositorioAplicacao {
	@Autowired
	InstrutorJpaRepository repositorio;

	@Autowired
	JpaMapeador mapeador;

	@Override
	public void salvar(Instrutor instrutor) {
		var instrutorJpa = mapeador.map(instrutor, InstrutorJpa.class);
		repositorio.save(instrutorJpa);
	}

	@Override
	public Instrutor obter(InstrutorId id) {
		var instrutorJpa = repositorio.findById(id.getId()).get();
		return mapeador.map(instrutorJpa, Instrutor.class);
	}

	@Override
	public List<InstrutorResumo> pesquisarResumos() {
		return repositorio.findInstrutorResumoByOrderByNome();
	}
}
