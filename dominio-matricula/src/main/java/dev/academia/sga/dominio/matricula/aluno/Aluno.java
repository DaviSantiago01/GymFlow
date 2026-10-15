package dev.academia.sga.dominio.matricula.aluno;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import dev.academia.sga.dominio.compartilhado.AlunoId;

public class Aluno {
	private final AlunoId id;

	private String nome;
	private Email emailContato;

	public Aluno(String nome, Email emailContato) {
		id = null;

		setNome(nome);
		setEmailContato(emailContato);
	}

	public Aluno(AlunoId id, String nome, Email emailContato) {
		notNull(id, "O id não pode ser nulo");
		this.id = id;

		setNome(nome);
		setEmailContato(emailContato);
	}

	public AlunoId getId() {
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

	public void setEmailContato(Email emailContato) {
		notNull(emailContato, "O email de contato não pode ser nulo");

		this.emailContato = emailContato;
	}

	public Email getEmailContato() {
		return emailContato;
	}

	@Override
	public String toString() {
		return nome;
	}
}
