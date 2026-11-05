package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.ICredencialRepositorio;

@Configuration
public class CadastroConfiguracao {
    @Bean
    ICredencialRepositorio credenciais(JdbcTemplate banco, PasswordEncoder codificador) {
        return new CredencialRepositorio(banco, codificador);
    }
}
