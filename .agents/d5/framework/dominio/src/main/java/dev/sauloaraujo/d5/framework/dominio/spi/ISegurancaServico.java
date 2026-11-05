package dev.sauloaraujo.d5.framework.dominio.spi;

import java.util.List;

public interface ISegurancaServico {
	String getUsuario() throws NaoAutenticadoExcecao;

	List<String> getAutorizacoes() throws NaoAutenticadoExcecao;

	default boolean possuiAutorizacao(String autorizacao) throws NaoAutenticadoExcecao {
		return getAutorizacoes().contains(autorizacao);
	}
}