package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.recomendacao;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.spi.ICatalogoRecomendacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.spi.IRecomendacaoRepositorio;

@Configuration
public class RecomendacoesConfiguracao {
    @Bean
    ICatalogoRecomendacaoRepositorio catalogoRecomendacao(JdbcTemplate banco) {
        return new CatalogoRecomendacaoRepositorio(banco);
    }

    @Bean @ConditionalOnProperty(name="treino.recomendacoes.agendamento", havingValue="true", matchIfMissing=true)
    ExpiracaoRecomendacoes expiracaoRecomendacoes(IRecomendacaoRepositorio recomendacoes, Clock relogio,
            PlatformTransactionManager gerenciador) {
        return new ExpiracaoRecomendacoes(recomendacoes, relogio, new TransactionTemplate(gerenciador));
    }
}
