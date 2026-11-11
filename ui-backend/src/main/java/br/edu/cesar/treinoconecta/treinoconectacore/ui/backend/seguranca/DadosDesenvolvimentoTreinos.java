package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.seguranca;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Personais e catálogo de exercícios. Carregados fora de produção, também sem perfil ativo,
 *  para que o personal consiga entrar ao executar com mvn spring-boot:test-run. */
@Configuration @Profile("!producao")
public class DadosDesenvolvimentoTreinos {
    private static final String[][] PERSONAIS = {
        {"p", "Rafael Souza"},
        {"q", "Camila Rocha"},
    };
    private static final Object[][] EXERCICIOS = {
        {"Supino reto", "PEITO", "200.00", "2.50"},
        {"Crucifixo", "PEITO", "80.00", null},
        {"Remada curvada", "COSTAS", "160.00", null},
        {"Puxada frontal", "COSTAS", "150.00", "2.50"},
        {"Agachamento livre", "PERNAS", "300.00", "5.00"},
        {"Leg press", "PERNAS", "500.00", null},
        {"Desenvolvimento", "OMBROS", "100.00", null},
        {"Rosca direta", "BRACOS", "70.00", "1.00"},
        {"Tríceps na polia", "BRACOS", "90.00", null},
        {"Abdominal na máquina", "ABDOMEN", "100.00", null},
    };

    @Bean ApplicationRunner cadastrarPersonaisEExercicios(JdbcTemplate banco, PasswordEncoder senhas,
            PlatformTransactionManager gerenciador) {
        return argumentos -> new TransactionTemplate(gerenciador).executeWithoutResult(estado -> {
            for (var personal : PERSONAIS) {
                cadastrarPersonal(banco, senhas, personal[0], personal[1]);
            }
            for (var exercicio : EXERCICIOS) {
                cadastrarExercicio(banco, exercicio);
            }
        });
    }

    private static void cadastrarPersonal(JdbcTemplate banco, PasswordEncoder senhas, String usuario, String nome) {
        Long existentes = banco.queryForObject("select count(*) from PER_PERSONAL where PER_USUARIO = ?", Long.class, usuario);
        if (Long.valueOf(0).equals(existentes)) {
            banco.update("insert into PER_PERSONAL(PER_USUARIO,PER_NOME) values (?,?)", usuario, nome);
            banco.update("insert into PCD_CREDENCIAL_PERSONAL(PER_ID,PCD_SENHA) select PER_ID,? from PER_PERSONAL where PER_USUARIO = ?",
                    senhas.encode(usuario), usuario);
        }
    }

    private static void cadastrarExercicio(JdbcTemplate banco, Object[] exercicio) {
        Long existentes = banco.queryForObject("select count(*) from EXE_EXERCICIO where EXE_NOME = ?", Long.class, exercicio[0]);
        if (Long.valueOf(0).equals(existentes)) {
            banco.update("insert into EXE_EXERCICIO(EXE_NOME,EXE_CATEGORIA,EXE_CARGA_MAXIMA,EXE_INCREMENTO_CARGA) values (?,?,CAST(? AS DECIMAL(7,2)),CAST(? AS DECIMAL(7,2)))",
                    exercicio);
        }
    }
}
