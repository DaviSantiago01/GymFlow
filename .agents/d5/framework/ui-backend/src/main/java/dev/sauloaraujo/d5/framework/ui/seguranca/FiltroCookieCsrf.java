package dev.sauloaraujo.d5.framework.ui.seguranca;

import java.io.IOException;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FiltroCookieCsrf extends OncePerRequestFilter {
	@Override
	protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
			throws ServletException, IOException {
		var token = (CsrfToken) requisicao.getAttribute(CsrfToken.class.getName());
		if (token != null) {
			token.getToken();
		}
		cadeia.doFilter(requisicao, resposta);
	}
}