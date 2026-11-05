package dev.sauloaraujo.d5.framework.ui.seguranca;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

public class EntradaAutenticacaoSemDesafio implements AuthenticationEntryPoint {
	private static final ObjectMapper JSON = new ObjectMapper();

	@Override
	public void commence(HttpServletRequest requisicao, HttpServletResponse resposta, AuthenticationException excecao)
			throws IOException {
		resposta.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		resposta.setContentType("application/json");
		JSON.writeValue(resposta.getOutputStream(), Map.of("mensagem", "Usuário não autenticado."));
	}
}