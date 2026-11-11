package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal;

import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

public final class Personal implements IAgregado<PersonalIdentificador> {
    public static final int TAMANHO_USUARIO = 100;
    public static final int TAMANHO_NOME = 100;

    private final Optional<PersonalIdentificador> identificador;
    private final String usuario;
    private final String nome;

    public Personal(Optional<PersonalIdentificador> identificador, String usuario, String nome) {
        this.identificador = Objects.requireNonNull(identificador);
        this.usuario = Validate.notBlank(usuario, PersonalServico.USUARIO_INVALIDO);
        this.nome = Validate.notBlank(nome, PersonalServico.NOME_INVALIDO);
        Validate.isTrue(usuario.length() <= TAMANHO_USUARIO, PersonalServico.USUARIO_INVALIDO);
        Validate.isTrue(nome.length() <= TAMANHO_NOME, PersonalServico.NOME_INVALIDO);
    }

    public Optional<PersonalIdentificador> getIdentificador() { return identificador; }
    public String getUsuario() { return usuario; }
    public String getNome() { return nome; }
}
