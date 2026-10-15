package dev.academia.sga;

import static org.springframework.boot.SpringApplication.run;

import java.io.IOException;

import org.springframework.boot.autoconfigure.SpringBootApplication;

// os beans de cada contexto ficam nas classes *Configuracao da infraestrutura
@SpringBootApplication
public class BackendAplicacao {
	public static void main(String[] args) throws IOException {
		run(BackendAplicacao.class, args);
	}
}
