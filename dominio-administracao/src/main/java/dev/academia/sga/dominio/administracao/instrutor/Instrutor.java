package dev.academia.sga.dominio.administracao.instrutor;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

public class Instrutor {
	private final InstrutorId id;

	private String nome;
	private String especialidade;

	public Instrutor(String nome, String especialidade) {
		id = null;

		setNome(nome);
		setEspecialidade(especialidade);
	}

	public Instrutor(InstrutorId id, String nome, String especialidade) {
		notNull(id, "O id não pode ser nulo");
		this.id = id;

		setNome(nome);
		setEspecialidade(especialidade);
	}

	public InstrutorId getId() {
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

	public void setEspecialidade(String especialidade) {
		if (especialidade != null) {
			notBlank(especialidade, "A especialidade não pode estar em branco");
		}

		this.especialidade = especialidade;
	}

	public String getEspecialidade() {
		return especialidade;
	}

	@Override
	public String toString() {
		return nome;
	}
}
