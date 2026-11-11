package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;

public record ExercicioExecutado(ExercicioIdentificador identificador, String nome) {
    public ExercicioExecutado(Long identificador, String nome) {
        this(new ExercicioIdentificador(identificador.toString()), nome);
    }
}
