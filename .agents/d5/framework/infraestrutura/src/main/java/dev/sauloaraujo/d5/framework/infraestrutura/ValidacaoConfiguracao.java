package dev.sauloaraujo.d5.framework.infraestrutura;

import java.util.Locale;

import org.hibernate.cfg.ValidationSettings;
import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

@Configuration
public class ValidacaoConfiguracao implements DisposableBean {
	private final ValidatorFactory fabricaValidador = Validation.byProvider(HibernateValidator.class).configure()
			.defaultLocale(Locale.of("pt", "BR")).buildValidatorFactory();

	@Bean
	HibernatePropertiesCustomizer validacaoEntidadesJpa() {
		return propriedades -> propriedades.put(ValidationSettings.JAKARTA_VALIDATION_FACTORY, fabricaValidador);
	}

	@Override
	public void destroy() {
		fabricaValidador.close();
	}
}
