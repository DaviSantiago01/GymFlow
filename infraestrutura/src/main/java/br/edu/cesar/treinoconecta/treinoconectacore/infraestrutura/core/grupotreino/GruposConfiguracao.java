package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.grupotreino;

import java.time.Clock;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.spi.IGrupoTreinoRepositorio;
import org.springframework.context.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
@Configuration
public class GruposConfiguracao {
    @Bean @ConditionalOnProperty(name="treino.grupos.agendamento",havingValue="true",matchIfMissing=true)
    ExpiracaoConvites expiracaoConvites(IGrupoTreinoRepositorio grupos,Clock relogio,PlatformTransactionManager gerenciador){return new ExpiracaoConvites(grupos,relogio,new TransactionTemplate(gerenciador));}
}
