package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio;

import java.util.Objects;
import java.util.Optional;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class ExercicioConsulta extends Consulta<ExercicioOrdenacao> {
    private final Optional<String> nome;

    public ExercicioConsulta(Optional<ExercicioOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina, Optional<String> nome) {
        super(ordenacao, descendente, indicePagina, tamanhoPagina);
        this.nome = Objects.requireNonNull(nome);
    }

    public Optional<String> getNome() { return nome; }
}
