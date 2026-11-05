package dev.sauloaraujo.d5.framework.ui;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorStringAbstrato;

@Configuration
public class JacksonConfiguracao {
	@Bean
	JsonMapperBuilderCustomizer mixinObjetoValorString() {
		return builder -> builder.addMixIn(ObjetoValorStringAbstrato.class, ObjetoValorStringMixin.class);
	}
}