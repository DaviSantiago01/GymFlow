package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.spi;

import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicioIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IExecucaoDeExercicioRepositorio
        extends IAgregadoRepositorio<ExecucaoDeExercicioIdentificador, ExecucaoDeExercicio> {
    List<ExecucaoDeExercicio> buscarPorAlunoEExercicio(AlunoIdentificador aluno, ExercicioIdentificador exercicio);
}
