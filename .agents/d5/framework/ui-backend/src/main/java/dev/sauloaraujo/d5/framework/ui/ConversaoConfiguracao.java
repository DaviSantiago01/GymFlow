package dev.sauloaraujo.d5.framework.ui;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConversaoConfiguracao {
	@Bean
	ObjetoValorStringConversorFactory objetoValorStringConversorFactory() {
		return new ObjetoValorStringConversorFactory();
	}
}
