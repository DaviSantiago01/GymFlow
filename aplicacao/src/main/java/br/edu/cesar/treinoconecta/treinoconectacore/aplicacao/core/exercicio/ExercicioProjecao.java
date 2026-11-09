package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio;

import java.math.BigDecimal;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.CategoriaExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.Exercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public record ExercicioProjecao(ExercicioIdentificador identificador, String nome, CategoriaExercicio categoria,
        BigDecimal cargaMaxima, Optional<BigDecimal> incrementoCarga) implements IProjecao<Exercicio> {
    public ExercicioProjecao(Long identificador, String nome, CategoriaExercicio categoria, BigDecimal cargaMaxima,
            @Nullable BigDecimal incrementoCarga) {
        this(new ExercicioIdentificador(identificador.toString()), nome, categoria, cargaMaxima,
                Optional.ofNullable(incrementoCarga));
    }
}
