package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio;

import java.util.Objects;
import java.util.Optional;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class ExecucaoDeExercicioConsulta extends Consulta<ExecucaoDeExercicioOrdenacao> {
    private final Optional<String> aluno;
    private final Optional<String> exercicio;

    public ExecucaoDeExercicioConsulta(Optional<ExecucaoDeExercicioOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina, Optional<String> aluno,
            Optional<String> exercicio) {
        super(ordenacao, descendente, indicePagina, tamanhoPagina);
        this.aluno = Objects.requireNonNull(aluno);
        this.exercicio = Objects.requireNonNull(exercicio);
    }

    public Optional<String> getAluno() { return aluno; }
    public Optional<String> getExercicio() { return exercicio; }
}
