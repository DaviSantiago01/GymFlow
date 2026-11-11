package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.Exercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IExercicioRepositorio extends IAgregadoRepositorio<ExercicioIdentificador, Exercicio> {
}
