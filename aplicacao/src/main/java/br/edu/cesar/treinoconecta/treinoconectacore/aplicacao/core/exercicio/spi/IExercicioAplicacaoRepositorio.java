package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.ExercicioConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.ExercicioProjecao;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IExercicioAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<ExercicioProjecao, ExercicioConsulta> {
}
