package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.seguranca;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@Profile("desenvolvimento & !producao")
public class CenarioDemonstracao {
    private final JdbcTemplate banco;
    private final TransactionTemplate transacao;

    public CenarioDemonstracao(JdbcTemplate banco, PlatformTransactionManager gerenciador) {
        this.banco = Objects.requireNonNull(banco);
        this.transacao = new TransactionTemplate(Objects.requireNonNull(gerenciador));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void carregar() {
        banco.execute("CREATE TABLE IF NOT EXISTS DEM_CENARIO (DEM_NOME VARCHAR(60) PRIMARY KEY)");
        carregarUmaVez("gymflow", "demonstracao/cenario.sql");
        carregarUmaVez("gymflow-equipamentos", "demonstracao/equipamentos.sql");
    }

    private void carregarUmaVez(String nome, String recurso) {
        var dados = new EncodedResource(new ClassPathResource(recurso), StandardCharsets.UTF_8);
        transacao.executeWithoutResult(estado -> {
            int aplicado = banco.update("""
                    INSERT INTO DEM_CENARIO (DEM_NOME)
                    SELECT ? WHERE NOT EXISTS (SELECT 1 FROM DEM_CENARIO WHERE DEM_NOME = ?)
                    """, nome, nome);
            if (aplicado == 0) return;

            // As contas, os exercícios e os catálogos já foram carregados pelos ApplicationRunners.
            banco.execute((ConnectionCallback<Boolean>) conexao -> {
                ScriptUtils.executeSqlScript(conexao, dados);
                return true;
            });
        });
    }
}
