package com.example.exemplo.ui.backend.seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import com.example.exemplo.dominio.Autorizacao;

import dev.sauloaraujo.d5.framework.ui.seguranca.SegurancaConfiguracaoAbstrata;

@Configuration
public class SegurancaConfiguracao extends SegurancaConfiguracaoAbstrata {
	@Override
	protected void autorizar(
			AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry autorizacao) {
		autorizacao.requestMatchers(BACKEND).authenticated();
	}

	@Bean
	UserDetailsService usuarios(PasswordEncoder codificadorSenha) {
		var administrador = User.withUsername("a").password(codificadorSenha.encode("a"))
				.authorities(Autorizacao.ADMINISTRADOR.name()).build();

		var usuario = User.withUsername("u").password(codificadorSenha.encode("u"))
				.authorities(Autorizacao.USUARIO.name()).build();

		return new InMemoryUserDetailsManager(administrador, usuario);
	}
}