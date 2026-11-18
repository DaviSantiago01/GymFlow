package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.seguranca;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Conta de administrador e exemplos de planos e profissionais, somente no perfil de desenvolvimento. Em produção, os
 * administradores são provisionados pela academia diretamente na tabela ADM_ADMINISTRADOR.
 */
@Configuration @Profile("desenvolvimento & !producao")
public class DadosDesenvolvimentoAdministracao {
    private static final Object[][] PLANOS = {
        {"Mensal", "99.90", 1},
        {"Trimestral", "269.90", 3},
        {"Anual", "959.90", 12},
    };
    private static final Object[][] PROFISSIONAIS = {
        {"Bruno Almeida", "52998224725", "012345-G/PE", "bruno.almeida@academia.com", true, true},
        {"Larissa Menezes", "39053344705", "067890-G/PE", "(81) 98888-1234", true, false},
    };

    @Bean ApplicationRunner cadastrarAdministracao(JdbcTemplate banco, PasswordEncoder senhas,
            PlatformTransactionManager gerenciador) {
        return argumentos -> new TransactionTemplate(gerenciador).executeWithoutResult(estado -> {
            cadastrarAdministrador(banco, senhas, "admin", "Administração");
            cadastrarAdministrador(banco, senhas, "gestor", "Gestor");
            for (var plano : PLANOS) {
                if (Long.valueOf(0).equals(banco.queryForObject("select count(*) from PLA_PLANO where PLA_NOME = ?", Long.class, plano[0]))) {
                    banco.update("insert into PLA_PLANO(PLA_NOME,PLA_VALOR,PLA_DURACAO_MESES,PLA_SITUACAO) values (?,CAST(? AS DECIMAL(7,2)),?,'ATIVO')",
                            plano);
                }
            }
            for (var profissional : PROFISSIONAIS) {
                if (Long.valueOf(0).equals(banco.queryForObject("select count(*) from PRF_PROFISSIONAL where PRF_CPF = ?", Long.class, profissional[1]))) {
                    banco.update("insert into PRF_PROFISSIONAL(PRF_NOME,PRF_CPF,PRF_CREF,PRF_CONTATO,PRF_PROFESSOR,PRF_PERSONAL_TRAINER,PRF_SITUACAO) values (?,?,?,?,?,?,'ATIVO')",
                            profissional);
                }
            }
        });
    }

    private static void cadastrarAdministrador(JdbcTemplate banco, PasswordEncoder senhas, String usuario, String nome) {
        if (Long.valueOf(0).equals(banco.queryForObject(
                "select count(*) from ADM_ADMINISTRADOR where ADM_USUARIO = ?", Long.class, usuario))) {
            banco.update("insert into ADM_ADMINISTRADOR(ADM_USUARIO,ADM_NOME,ADM_SENHA) values (?,?,?)",
                    usuario, nome, senhas.encode(usuario));
        }
    }
}
