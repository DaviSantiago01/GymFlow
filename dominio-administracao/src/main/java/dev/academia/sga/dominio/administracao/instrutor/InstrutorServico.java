package dev.academia.sga.dominio.administracao.instrutor;

import static org.apache.commons.lang3.Validate.notNull;

import dev.academia.sga.dominio.compartilhado.InstrutorId;

public class InstrutorServico {
	private final InstrutorRepositorio instrutorRepositorio;

	public InstrutorServico(InstrutorRepositorio instrutorRepositorio) {
		notNull(instrutorRepositorio, "O repositório de instrutores não pode ser nulo");

		this.instrutorRepositorio = instrutorRepositorio;
	}

	public void salvar(Instrutor instrutor) {
		notNull(instrutor, "O instrutor não pode ser nulo");

		instrutorRepositorio.salvar(instrutor);
	}

	public Instrutor obter(InstrutorId id) {
		notNull(id, "O id do instrutor não pode ser nulo");

		return instrutorRepositorio.obter(id);
	}
}
