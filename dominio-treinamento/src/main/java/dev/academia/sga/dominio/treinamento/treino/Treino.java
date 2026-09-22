package dev.academia.sga.dominio.treinamento.treino;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.academia.sga.dominio.administracao.aluno.AlunoId;
import dev.academia.sga.dominio.administracao.instrutor.InstrutorId;
import dev.academia.sga.dominio.treinamento.exercicio.ExercicioId;

public class Treino {
	private final TreinoId id;

	private String nome;
	private String objetivo;

	private AlunoId aluno;
	private InstrutorId instrutor;

	private List<ExercicioId> exercicios = new ArrayList<>();

	public Treino(TreinoId id, String nome, String objetivo, AlunoId aluno, InstrutorId instrutor,
			List<ExercicioId> exercicios) {
		notNull(id, "O id não pode ser nulo");
		this.id = id;

		setNome(nome);
		setObjetivo(objetivo);
		setAluno(aluno);
		setInstrutor(instrutor);
		setExercicios(exercicios);
	}

	public TreinoId getId() {
		return id;
	}

	public void setNome(String nome) {
		notNull(nome, "O nome não pode ser nulo");
		notBlank(nome, "O nome não pode estar em branco");

		this.nome = nome;
	}

	public String getNome() {
		return nome;
	}

	public void setObjetivo(String objetivo) {
		if (objetivo != null) {
			notBlank(objetivo, "O objetivo não pode estar em branco");
		}

		this.objetivo = objetivo;
	}

	public String getObjetivo() {
		return objetivo;
	}

	public void setAluno(AlunoId aluno) {
		notNull(aluno, "O aluno não pode ser nulo");

		this.aluno = aluno;
	}

	public AlunoId getAluno() {
		return aluno;
	}

	public void setInstrutor(InstrutorId instrutor) {
		notNull(instrutor, "O instrutor não pode ser nulo");

		this.instrutor = instrutor;
	}

	public InstrutorId getInstrutor() {
		return instrutor;
	}

	private void setExercicios(Collection<ExercicioId> exercicios) {
		notNull(exercicios, "O vetor de exercícios não pode ser nulo");
		notEmpty(exercicios, "O treino deve ter pelo menos um exercício");

		for (var exercicio : exercicios) {
			adicionarExercicio(exercicio);
		}
	}

	public Collection<ExercicioId> getExercicios() {
		var copia = new ArrayList<ExercicioId>();
		copia.addAll(exercicios);
		return copia;
	}

	public void adicionarExercicio(ExercicioId exercicio) {
		notNull(exercicio, "O exercício não pode ser nulo");

		exercicios.add(exercicio);
	}

	@Override
	public String toString() {
		return nome;
	}
}
