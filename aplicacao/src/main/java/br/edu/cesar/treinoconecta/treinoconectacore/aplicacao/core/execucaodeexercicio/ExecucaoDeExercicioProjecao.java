package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio;

import java.math.BigDecimal;
import java.time.Instant;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public record ExecucaoDeExercicioProjecao(ExecucaoDeExercicioIdentificador identificador, AlunoIdentificador aluno,
        String alunoNome, PlanoDeTreinoIdentificador plano, String planoNome, DiaSemana dia,
        ExercicioIdentificador exercicio, String exercicioNome, Instant data, int series, int repeticoes, BigDecimal carga)
        implements IProjecao<ExecucaoDeExercicio> {
    public ExecucaoDeExercicioProjecao(Long identificador, Long aluno, String alunoNome, Long plano, String planoNome,
            DiaSemana dia, Long exercicio, String exercicioNome, Instant data, int series, int repeticoes,
            BigDecimal carga) {
        this(new ExecucaoDeExercicioIdentificador(identificador.toString()), new AlunoIdentificador(aluno.toString()),
                alunoNome, new PlanoDeTreinoIdentificador(plano.toString()), planoNome, dia,
                new ExercicioIdentificador(exercicio.toString()), exercicioNome, data, series, repeticoes, carga);
    }
}
