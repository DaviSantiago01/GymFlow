package dev.academia.sga.dominio.administracao.instrutor;

import dev.academia.sga.dominio.compartilhado.InstrutorId;

public interface InstrutorRepositorio {
	void salvar(Instrutor instrutor);

	Instrutor obter(InstrutorId id);
}
