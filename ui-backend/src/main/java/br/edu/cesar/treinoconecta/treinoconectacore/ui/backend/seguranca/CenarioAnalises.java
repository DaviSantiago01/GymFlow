package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.seguranca;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Exemplos dos relatórios e das recomendações. O catálogo de aulas e exercícios e o perfil da conta "aluno" entram em
 * todo ambiente de desenvolvimento. O movimento fictício da academia, com dezenas de alunos, só entra com o perfil
 * "demonstracao", para não alterar os exemplos usados pelos testes das demais funcionalidades.
 */
@Component
@Profile("desenvolvimento & !producao")
public class CenarioAnalises {
    private final JdbcTemplate banco;
    private final TransactionTemplate transacao;
    private final CenarioDemonstracao cenario;
    private final Environment ambiente;

    public CenarioAnalises(JdbcTemplate banco, PlatformTransactionManager gerenciador, CenarioDemonstracao cenario,
            Environment ambiente) {
        this.banco = Objects.requireNonNull(banco);
        this.transacao = new TransactionTemplate(Objects.requireNonNull(gerenciador));
        this.cenario = Objects.requireNonNull(cenario);
        this.ambiente = Objects.requireNonNull(ambiente);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void carregar() {
        cenario.carregar();
        carregarUmaVez("gymflow-recomendacoes", "demonstracao/recomendacoes.sql");
        if (ambiente.matchesProfiles("demonstracao")) {
            carregarUmaVez("gymflow-academia", "demonstracao/academia.sql");
            completarContatos();
        }
    }

    /** CPF e telefone fictícios para os alunos de exemplo, usados na exportação mascarada dos relatórios. */
    private void completarContatos() {
        var alunos = banco.queryForList("""
                SELECT ALU_ID, CAST(SUBSTRING(ALU_USUARIO, 5) AS INT) AS N FROM ALU_ALUNO
                 WHERE ALU_USUARIO LIKE 'demo__' AND ALU_CPF IS NULL
                """);
        for (var aluno : alunos) {
            int numero = ((Number) Objects.requireNonNull(aluno.get("N"))).intValue();
            var cpf = cpf(String.format("%09d", 318_204_000L + numero * 7_919L));
            if (Integer.valueOf(0).equals(banco.queryForObject("SELECT COUNT(*) FROM ALU_ALUNO WHERE ALU_CPF = ?",
                    Integer.class, cpf))) {
                banco.update("UPDATE ALU_ALUNO SET ALU_CPF = ?, ALU_TELEFONE = COALESCE(ALU_TELEFONE, ?) WHERE ALU_ID = ?", cpf,
                        String.format("8198%07d", numero * 48_271 % 10_000_000), aluno.get("ALU_ID"));
            }
        }
    }

    private static String cpf(String base) {
        var digitos = new StringBuilder(base);
        for (int tamanho = 9; tamanho <= 10; tamanho++) {
            int soma = 0;
            for (int indice = 0; indice < tamanho; indice++) {
                soma += (digitos.charAt(indice) - '0') * (tamanho + 1 - indice);
            }
            int resto = soma % 11;
            digitos.append(resto < 2 ? 0 : 11 - resto);
        }
        return digitos.toString();
    }

    private void carregarUmaVez(String nome, String recurso) {
        var dados = new EncodedResource(new ClassPathResource(recurso), StandardCharsets.UTF_8);
        transacao.executeWithoutResult(estado -> {
            int aplicado = banco.update("""
                    INSERT INTO DEM_CENARIO (DEM_NOME)
                    SELECT ? WHERE NOT EXISTS (SELECT 1 FROM DEM_CENARIO WHERE DEM_NOME = ?)
                    """, nome, nome);
            if (aplicado == 0) return;
            banco.execute((ConnectionCallback<Boolean>) conexao -> {
                ScriptUtils.executeSqlScript(conexao, dados);
                return true;
            });
        });
    }
}
