package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.seguranca;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
@Configuration @Profile("desenvolvimento & !producao")
public class DadosDesenvolvimento {
    @Bean ApplicationRunner cadastrarExemplos(JdbcTemplate banco, PasswordEncoder senhas, PlatformTransactionManager gerenciador) {
        return argumentos -> new TransactionTemplate(gerenciador).executeWithoutResult(estado -> {
            for (String usuario : new String[]{"u","v"}) {
                if (Long.valueOf(0).equals(banco.queryForObject("select count(*) from ALU_ALUNO where ALU_USUARIO = ?",Long.class,usuario))) {
                    banco.update("insert into ALU_ALUNO(ALU_USUARIO,ALU_NOME) values (?,?)",usuario,usuario.equals("u")?"Mariana Costa":"Pedro Silva");
                    banco.update("insert into USU_CREDENCIAL(ALU_USUARIO,USU_SENHA) values (?,?)",usuario,senhas.encode(usuario));
                }
            }
            for (int i=1;i<=6;i++) {
                String codigo="A-%02d".formatted(i);
                if (Long.valueOf(0).equals(banco.queryForObject("select count(*) from ARM_ARMARIO where ARM_CODIGO = ?",Long.class,codigo)))
                    banco.update("insert into ARM_ARMARIO values (?,?,?)",codigo,"Ala Vestiário A · Piso térreo",switch(i){case 2 -> "Próximo à entrada"; case 3 -> "Próximo ao espelho"; case 4,5 -> "Superior / Alto"; case 6 -> "Inferior"; default -> "Tamanho padrão";});
            }
        });
    }
}
