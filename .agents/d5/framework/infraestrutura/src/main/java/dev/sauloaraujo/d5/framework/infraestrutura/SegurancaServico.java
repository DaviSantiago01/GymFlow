package dev.sauloaraujo.d5.framework.infraestrutura;

import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutenticadoExcecao;

public class SegurancaServico implements ISegurancaServico {
	@Override
	public String getUsuario() {
		return getAuthentication().getName();
	}

	private Authentication getAuthentication() {
		var contexto = SecurityContextHolder.getContext();
		var autenticacao = contexto.getAuthentication();
		if (autenticacao == null || !autenticacao.isAuthenticated()
				|| autenticacao instanceof AnonymousAuthenticationToken) {
			throw new NaoAutenticadoExcecao("Usuário não autenticado");
		}
		return autenticacao;
	}

	@Override
	public List<String> getAutorizacoes() {
		return getAuthentication().getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
	}
}