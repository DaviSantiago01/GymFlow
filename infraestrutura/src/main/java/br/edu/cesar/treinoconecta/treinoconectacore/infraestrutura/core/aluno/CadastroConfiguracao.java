package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.spi.IAlunoGestaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoVinculosRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.ICredencialRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IHistoricoAlunoRepositorio;
import jakarta.persistence.EntityManager;

/** Adaptadores do aluno que não são repositórios de agregado e, por isso, não são registrados pelo D5. */
@Configuration
public class CadastroConfiguracao {
    @Bean
    ICredencialRepositorio credenciais(JdbcTemplate banco, PasswordEncoder codificador) {
        return new CredencialRepositorio(banco, codificador);
    }

    @Bean
    IHistoricoAlunoRepositorio historicoAlunos(IHistoricoAlunoRepositorioJpa jpa, EntityManager em) {
        return new HistoricoAlunoRepositorio(jpa, em);
    }

    @Bean
    IAlunoVinculosRepositorio vinculosAlunos(JdbcTemplate banco) {
        return new AlunoVinculosRepositorio(banco);
    }

    @Bean
    IAlunoGestaoRepositorio gestaoAlunos(IAlunoGestaoJpa alunos, IHistoricoAlunoRepositorioJpa historico) {
        return new AlunoGestaoRepositorio(alunos, historico);
    }
}
