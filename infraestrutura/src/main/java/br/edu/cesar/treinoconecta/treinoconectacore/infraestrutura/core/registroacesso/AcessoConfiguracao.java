package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.registroacesso;

import java.time.Duration;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.ParametrosAcesso;

/** Parâmetros do controle de acesso (F006), configuráveis em application.properties. */
@Configuration
public class AcessoConfiguracao {
    @Bean
    ParametrosAcesso parametrosAcesso(@Value("${treino.acesso.capacidade-maxima:100}") int capacidade,
            @Value("${treino.acesso.permanencia-maxima-minutos:240}") long permanencia,
            @Value("${treino.acesso.abertura:06:00}") String abertura,
            @Value("${treino.acesso.fechamento:22:00}") String fechamento) {
        return new ParametrosAcesso(capacidade, Duration.ofMinutes(permanencia), LocalTime.parse(abertura),
                LocalTime.parse(fechamento));
    }
}
