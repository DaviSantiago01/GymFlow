package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.ICredencialRepositorio;

public final class CredencialRepositorio implements ICredencialRepositorio {
    private final JdbcTemplate banco;
    private final PasswordEncoder codificador;

    public CredencialRepositorio(JdbcTemplate banco, PasswordEncoder codificador) {
        this.banco = Objects.requireNonNull(banco);
        this.codificador = Objects.requireNonNull(codificador);
    }

    public void incluir(String usuario, String senha) {
        Objects.requireNonNull(usuario);
        Objects.requireNonNull(senha);
        banco.update("insert into USU_CREDENCIAL(ALU_USUARIO,USU_SENHA) values (?,?)",
                usuario, codificador.encode(senha));
    }
}
