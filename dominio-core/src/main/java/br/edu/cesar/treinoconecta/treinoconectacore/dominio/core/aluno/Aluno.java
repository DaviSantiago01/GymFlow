package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno;

import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

public final class Aluno implements IAgregado<AlunoIdentificador> {
    private final Optional<AlunoIdentificador> identificador;
    private final String usuario;
    private final String nome;
    public Aluno(Optional<AlunoIdentificador> identificador, String usuario, String nome) {
        this.identificador = Objects.requireNonNull(identificador);
        this.usuario = Validate.notBlank(usuario, "Usuário obrigatório");
        this.nome = Validate.notBlank(nome, "Nome obrigatório");
        Validate.isTrue(usuario.length() <= 100 && nome.length() <= 100, "Nome ou usuário muito longo");
    }
    public Optional<AlunoIdentificador> getIdentificador() { return identificador; }
    public String getUsuario() { return usuario; }
    public String getNome() { return nome; }
}
