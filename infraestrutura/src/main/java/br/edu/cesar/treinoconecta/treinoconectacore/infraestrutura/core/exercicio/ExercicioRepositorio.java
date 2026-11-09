package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio;

import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.Exercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.spi.IExercicioRepositorio;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class ExercicioRepositorio extends AgregadoRepositorioAbstrato<ExercicioIdentificador, Exercicio, Long, ExercicioJpa>
        implements IExercicioRepositorio {

    public ExercicioRepositorio(IExercicioRepositorioJpa repositorioJpa) {
        super(repositorioJpa, ExercicioServico.IDENTIFICADOR_INEXISTENTE_MODELO);
    }

    @Override
    protected Long paraIdentificadorJpa(ExercicioIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected ExercicioJpa paraJpa(Exercicio agregado) {
        var identificador = agregado.getIdentificador().map(this::paraIdentificadorJpa).orElse(null);
        return new ExercicioJpa(identificador, agregado.getNome(), agregado.getCategoria(), agregado.getCargaMaxima(),
                agregado.getIncrementoCarga().orElse(null));
    }

    @Override
    protected Exercicio paraAgregado(ExercicioJpa entidadeJpa) {
        var identificador = new ExercicioIdentificador(entidadeJpa.getIdentificador().toString());
        return new Exercicio(Optional.of(identificador), entidadeJpa.getNome(), entidadeJpa.getCategoria(),
                entidadeJpa.getCargaMaxima(), Optional.ofNullable(entidadeJpa.getIncrementoCarga()));
    }
}
