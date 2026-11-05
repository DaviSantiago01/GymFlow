package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core;

import java.time.Clock;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.spi.IReservaArmarioRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.reserva.ExpiracaoReservas;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
@Configuration @EnableScheduling
public class ArmariosConfiguracao {
    @Bean @ConditionalOnMissingBean(Clock.class)
    Clock relogio() { return Clock.systemUTC(); }
    @Bean @ConditionalOnProperty(name="treino.reservas.agendamento",havingValue="true",matchIfMissing=true)
    ExpiracaoReservas expiracaoReservas(IReservaArmarioRepositorio reservas, Clock relogio, PlatformTransactionManager gerenciador) {
        return new ExpiracaoReservas(reservas,relogio,new TransactionTemplate(gerenciador));
    }
}
