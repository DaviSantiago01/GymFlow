package dev.academia.sga.dominio.treinamento.exercicio;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

public class Exercicio {
	private final ExercicioId id;

	private String nome;
	private String grupoMuscular;

	public Exercicio(String nome, String grupoMuscular) {
		id = null;

		setNome(nome);
		setGrupoMuscular(grupoMuscular);
	}

	public Exercicio(ExercicioId id, String nome, String grupoMuscular) {
		notNull(id, "O id não pode ser nulo");
		this.id = id;

		setNome(nome);
		setGrupoMuscular(grupoMuscular);
	}

	public ExercicioId getId() {
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

	public void setGrupoMuscular(String grupoMuscular) {
		notNull(grupoMuscular, "O grupo muscular não pode ser nulo");
		notBlank(grupoMuscular, "O grupo muscular não pode estar em branco");

		this.grupoMuscular = grupoMuscular;
	}

	public String getGrupoMuscular() {
		return grupoMuscular;
	}

	@Override
	public String toString() {
		return nome;
	}
}
