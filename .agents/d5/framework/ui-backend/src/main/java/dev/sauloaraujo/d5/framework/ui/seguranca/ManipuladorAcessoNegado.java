package dev.sauloaraujo.d5.framework.ui.seguranca;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

public class ManipuladorAcessoNegado implements AccessDeniedHandler {
	private static final ObjectMapper JSON = new ObjectMapper();

	@Override
	public void handle(HttpServletRequest requisicao, HttpServletResponse resposta, AccessDeniedException excecao)
			throws IOException {
		var mensagem = excecao instanceof CsrfException ? "Token CSRF ausente ou inválido." : "Ação não autorizada.";
		resposta.setStatus(HttpServletResponse.SC_FORBIDDEN);
		resposta.setContentType("application/json");
		JSON.writeValue(resposta.getOutputStream(), Map.of("mensagem", mensagem));
	}
}
