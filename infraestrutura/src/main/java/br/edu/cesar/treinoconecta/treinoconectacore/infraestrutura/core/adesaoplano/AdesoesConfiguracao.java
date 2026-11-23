package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.adesaoplano;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoServico;

@Configuration
public class AdesoesConfiguracao {
    @Bean @ConditionalOnProperty(name = "treino.adesoes.agendamento", havingValue = "true", matchIfMissing = true)
    ConclusaoAdesoes conclusaoAdesoes(AdesaoPlanoServico adesoes, PlatformTransactionManager gerenciador) {
        return new ConclusaoAdesoes(adesoes, new TransactionTemplate(gerenciador));
    }
}
