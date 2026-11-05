package dev.sauloaraujo.d5.framework.ui.seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import dev.sauloaraujo.d5.framework.ui.Backend;

public abstract class SegurancaConfiguracaoAbstrata {
	protected static final String BACKEND = "/" + Backend.PREFIXO + "**";

	protected static final String SAIR = "/" + Backend.PREFIXO + "sair";

	private static final String[] SWAGGER = { "/swagger-ui/favicon-16x16.png", "/swagger-ui/favicon-32x32.png",
			"/swagger-ui/index.css", "/swagger-ui/index.html", "/swagger-ui/swagger-initializer.js",
			"/swagger-ui/swagger-ui-bundle.js", "/swagger-ui/swagger-ui-standalone-preset.js",
			"/swagger-ui/swagger-ui.css", "/v3/api-docs", "/v3/api-docs.yaml", "/v3/api-docs/swagger-config" };

	@Bean
	public SecurityFilterChain filtroSeguranca(HttpSecurity http) {
		var entradaSemDesafio = new EntradaAutenticacaoSemDesafio();

		// @formatter:off
		return http.csrf(csrf -> csrf
						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
						.csrfTokenRequestHandler(new ManipuladorRequisicaoCsrfSpa()))
				.addFilterAfter(new FiltroCookieCsrf(), BasicAuthenticationFilter.class)
				.sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
				.authorizeHttpRequests(autorizacao -> {
					autorizacao.requestMatchers(SWAGGER).permitAll();
					autorizar(autorizacao);
					autorizacao.requestMatchers(BACKEND).denyAll();
				})
				.httpBasic(basico -> basico.authenticationEntryPoint(entradaSemDesafio))
				.exceptionHandling(excecoes -> excecoes.accessDeniedHandler(new ManipuladorAcessoNegado()))
				.logout(logout -> logout
						.logoutUrl(SAIR)
						.logoutSuccessHandler((requisicao, resposta, autenticacao) -> resposta
								.setStatus(HttpStatus.NO_CONTENT.value())))
				.build();
		// @formatter:on
	}

	protected abstract void autorizar(
			AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry autorizacao);

	@Bean
	public PasswordEncoder codificadorSenha() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}