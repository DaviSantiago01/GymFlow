package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.spi;

import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Cpf;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Cref;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Profissional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IProfissionalRepositorio extends IAgregadoRepositorio<ProfissionalIdentificador, Profissional> {
    Profissional bloquear(ProfissionalIdentificador identificador);
    boolean existePorCpf(Cpf cpf, Optional<ProfissionalIdentificador> identificadorIgnorado);
    boolean existePorCref(Cref cref, Optional<ProfissionalIdentificador> identificadorIgnorado);
}
