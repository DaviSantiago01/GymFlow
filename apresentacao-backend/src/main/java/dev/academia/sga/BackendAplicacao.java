package dev.academia.sga;

import static org.springframework.boot.SpringApplication.run;

import java.io.IOException;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import dev.academia.sga.aplicacao.administracao.aluno.AlunoRepositorioAplicacao;
import dev.academia.sga.aplicacao.administracao.aluno.AlunoServicoAplicacao;
import dev.academia.sga.aplicacao.administracao.instrutor.InstrutorRepositorioAplicacao;
import dev.academia.sga.aplicacao.administracao.instrutor.InstrutorServicoAplicacao;
import dev.academia.sga.aplicacao.treinamento.exercicio.ExercicioRepositorioAplicacao;
import dev.academia.sga.aplicacao.treinamento.exercicio.ExercicioServicoAplicacao;
import dev.academia.sga.aplicacao.treinamento.treino.TreinoRepositorioAplicacao;
import dev.academia.sga.aplicacao.treinamento.treino.TreinoServicoAplicacao;
import dev.academia.sga.dominio.administracao.aluno.AlunoRepositorio;
import dev.academia.sga.dominio.administracao.aluno.AlunoServico;
import dev.academia.sga.dominio.administracao.instrutor.InstrutorRepositorio;
import dev.academia.sga.dominio.administracao.instrutor.InstrutorServico;
import dev.academia.sga.dominio.treinamento.exercicio.ExercicioRepositorio;
import dev.academia.sga.dominio.treinamento.exercicio.ExercicioServico;
import dev.academia.sga.dominio.treinamento.treino.TreinoRepositorio;
import dev.academia.sga.dominio.treinamento.treino.TreinoServico;

@SpringBootApplication
public class BackendAplicacao {
	@Bean
	public AlunoServico alunoServico(AlunoRepositorio repositorio) {
		return new AlunoServico(repositorio);
	}

	@Bean
	public AlunoServicoAplicacao alunoServicoAplicacao(AlunoRepositorioAplicacao repositorio) {
		return new AlunoServicoAplicacao(repositorio);
	}

	@Bean
	public InstrutorServico instrutorServico(InstrutorRepositorio repositorio) {
		return new InstrutorServico(repositorio);
	}

	@Bean
	public InstrutorServicoAplicacao instrutorServicoAplicacao(InstrutorRepositorioAplicacao repositorio) {
		return new InstrutorServicoAplicacao(repositorio);
	}

	@Bean
	public ExercicioServico exercicioServico(ExercicioRepositorio repositorio) {
		return new ExercicioServico(repositorio);
	}

	@Bean
	public ExercicioServicoAplicacao exercicioServicoAplicacao(ExercicioRepositorioAplicacao repositorio) {
		return new ExercicioServicoAplicacao(repositorio);
	}

	@Bean
	public TreinoServico treinoServico(TreinoRepositorio repositorio) {
		return new TreinoServico(repositorio);
	}

	@Bean
	public TreinoServicoAplicacao treinoServicoAplicacao(TreinoRepositorioAplicacao repositorio) {
		return new TreinoServicoAplicacao(repositorio);
	}

	public static void main(String[] args) throws IOException {
		run(BackendAplicacao.class, args);
	}
}
