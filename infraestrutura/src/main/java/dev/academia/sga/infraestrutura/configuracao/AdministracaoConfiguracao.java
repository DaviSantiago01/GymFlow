package dev.academia.sga.infraestrutura.configuracao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.academia.sga.aplicacao.administracao.instrutor.InstrutorRepositorioAplicacao;
import dev.academia.sga.aplicacao.administracao.instrutor.InstrutorServicoAplicacao;
import dev.academia.sga.dominio.administracao.instrutor.InstrutorRepositorio;
import dev.academia.sga.dominio.administracao.instrutor.InstrutorServico;

@Configuration
public class AdministracaoConfiguracao {
	@Bean
	public InstrutorServico instrutorServico(InstrutorRepositorio repositorio) {
		return new InstrutorServico(repositorio);
	}

	@Bean
	public InstrutorServicoAplicacao instrutorServicoAplicacao(InstrutorRepositorioAplicacao repositorio) {
		return new InstrutorServicoAplicacao(repositorio);
	}
}
