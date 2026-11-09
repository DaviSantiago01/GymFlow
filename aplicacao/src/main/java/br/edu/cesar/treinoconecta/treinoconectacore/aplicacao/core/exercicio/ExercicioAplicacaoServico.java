package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.spi.IExercicioAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.Exercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioServico;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class ExercicioAplicacaoServico extends
        AgregadoAplicacaoServicoAbstrato<ExercicioIdentificador, Exercicio, AgregadoAutorizacoes<Exercicio>, ExercicioProjecao, ExercicioConsulta> {

    public ExercicioAplicacaoServico(IExercicioAplicacaoRepositorio repositorio, ExercicioServico servico) {
        super(repositorio, servico);
    }
}
