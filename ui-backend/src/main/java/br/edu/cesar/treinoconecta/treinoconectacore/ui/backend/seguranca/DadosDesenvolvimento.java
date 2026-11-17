package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.seguranca;

import org.springframework.boot.ApplicationRunner;
import java.time.Clock;
import java.util.Objects;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.treino.spi.ITreinoDemonstracaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
@Configuration @Profile("desenvolvimento & !producao")
public class DadosDesenvolvimento {
    @Bean ApplicationRunner cadastrarExemplos(JdbcTemplate banco, PasswordEncoder senhas, PlatformTransactionManager gerenciador, ITreinoDemonstracaoRepositorio treinos, Clock relogio) {
        return argumentos -> new TransactionTemplate(gerenciador).executeWithoutResult(estado -> {
            for (String usuario : new String[]{"u","v"}) {
                if (Long.valueOf(0).equals(banco.queryForObject("select count(*) from ALU_ALUNO where ALU_USUARIO = ?",Long.class,usuario))) {
                    banco.update("insert into ALU_ALUNO(ALU_USUARIO,ALU_NOME) values (?,?)",usuario,usuario.equals("u")?"Mariana Costa":"Pedro Silva");
                    banco.update("insert into USU_CREDENCIAL(ALU_USUARIO,USU_SENHA) values (?,?)",usuario,senhas.encode(usuario));
                }
            }
            for (String nome : new String[]{"Carlos Lima","Juliana Alves","Ana Ribeiro"}) {
                if (Long.valueOf(0).equals(banco.queryForObject("select count(*) from PRO_PROFESSOR where PRO_NOME=? and PRO_DEMONSTRACAO=true",Long.class,nome)))
                    banco.update("insert into PRO_PROFESSOR(PRO_NOME,PRO_ESPECIALIDADE,PRO_BIOGRAFIA,PRO_DEMONSTRACAO) values (?,?,?,true)",nome,"Treinamento físico",
                        "Perfil de demonstração para testar avaliações. Não representa um cadastro real da academia.");
            }
            for (String usuario : new String[]{"u","v"}) {
                var id=Objects.requireNonNull(banco.queryForObject("select ALU_ID from ALU_ALUNO where ALU_USUARIO=?",Long.class,usuario));
                treinos.cadastrar(new AlunoIdentificador(id.toString()),relogio.instant());
            }
            for (int i=1;i<=6;i++) {
                String codigo="A-%02d".formatted(i);
                if (Long.valueOf(0).equals(banco.queryForObject("select count(*) from ARM_ARMARIO where ARM_CODIGO = ?",Long.class,codigo)))
                    banco.update("insert into ARM_ARMARIO values (?,?,?)",codigo,"Ala Vestiário A · Piso térreo",switch(i){case 2 -> "Próximo à entrada"; case 3 -> "Próximo ao espelho"; case 4,5 -> "Superior / Alto"; case 6 -> "Inferior"; default -> "Tamanho padrão";});
            }
        });
    }
}
