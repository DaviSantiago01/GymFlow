package dev.sauloaraujo.d5.framework.ui.seguranca;

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ManipuladorRequisicaoCsrfSpa extends CsrfTokenRequestAttributeHandler {
	private final CsrfTokenRequestAttributeHandler delegado = new XorCsrfTokenRequestAttributeHandler();

	@Override
	public void handle(HttpServletRequest requisicao, HttpServletResponse resposta, Supplier<CsrfToken> token) {
		delegado.handle(requisicao, resposta, token);
	}

	@Override
	@Nullable
	public String resolveCsrfTokenValue(HttpServletRequest requisicao, CsrfToken token) {
		var valorHeader = requisicao.getHeader(token.getHeaderName());
		return StringUtils.hasText(valorHeader) ? super.resolveCsrfTokenValue(requisicao, token)
				: delegado.resolveCsrfTokenValue(requisicao, token);
	}
}
