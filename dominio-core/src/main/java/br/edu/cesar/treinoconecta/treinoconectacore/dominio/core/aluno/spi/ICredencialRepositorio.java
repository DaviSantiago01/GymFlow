package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi;

public interface ICredencialRepositorio {
    /** O adaptador protege a senha antes de gravá-la. */
    void incluir(String usuario, String senha);
}
