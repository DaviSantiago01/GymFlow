package dev.academia.sga.dominio.administracao.instrutor;

public interface InstrutorRepositorio {
	void salvar(Instrutor instrutor);

	Instrutor obter(InstrutorId id);
}
