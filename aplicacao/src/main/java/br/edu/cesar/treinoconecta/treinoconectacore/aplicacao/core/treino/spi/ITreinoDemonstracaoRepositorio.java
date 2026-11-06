package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.treino.spi;

import java.time.Instant;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
public interface ITreinoDemonstracaoRepositorio {void cadastrar(AlunoIdentificador aluno,Instant agora);}
