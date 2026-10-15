package dev.academia.sga.infraestrutura.configuracao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.academia.sga.aplicacao.matricula.aluno.AlunoRepositorioAplicacao;
import dev.academia.sga.aplicacao.matricula.aluno.AlunoServicoAplicacao;
import dev.academia.sga.dominio.matricula.aluno.AlunoRepositorio;
import dev.academia.sga.dominio.matricula.aluno.AlunoServico;

// beans do contexto de Matricula; cada contexto tem a sua classe de configuracao
@Configuration
public class MatriculaConfiguracao {
	@Bean
	public AlunoServico alunoServico(AlunoRepositorio repositorio) {
		return new AlunoServico(repositorio);
	}

	@Bean
	public AlunoServicoAplicacao alunoServicoAplicacao(AlunoRepositorioAplicacao repositorio) {
		return new AlunoServicoAplicacao(repositorio);
	}
}
