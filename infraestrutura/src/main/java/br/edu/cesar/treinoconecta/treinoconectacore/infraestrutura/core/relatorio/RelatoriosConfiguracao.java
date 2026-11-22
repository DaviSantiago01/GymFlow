package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.relatorio;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.spi.IFonteDadosRelatorio;

@Configuration
public class RelatoriosConfiguracao {
    @Bean
    IFonteDadosRelatorio fonteDadosRelatorio(JdbcTemplate banco) {
        return new FonteDadosRelatorio(banco);
    }
}
