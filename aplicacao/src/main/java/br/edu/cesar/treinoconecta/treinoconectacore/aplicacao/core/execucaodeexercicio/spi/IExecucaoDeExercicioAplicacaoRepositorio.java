package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.spi;

import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExecucaoDeExercicioConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExecucaoDeExercicioProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExercicioExecutado;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicioIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IExecucaoDeExercicioAplicacaoRepositorio
        extends IAgregadoAplicacaoRepositorio<ExecucaoDeExercicioProjecao, ExecucaoDeExercicioConsulta> {
    ExecucaoDeExercicioProjecao projetar(ExecucaoDeExercicioIdentificador identificador);
    List<ExecucaoDeExercicioProjecao> listarCronologicamente(AlunoIdentificador aluno, ExercicioIdentificador exercicio);
    List<ExercicioExecutado> exerciciosExecutados(AlunoIdentificador aluno);
}
