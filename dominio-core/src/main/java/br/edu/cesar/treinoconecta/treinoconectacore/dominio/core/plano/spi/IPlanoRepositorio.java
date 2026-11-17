package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.spi;

import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.Plano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IPlanoRepositorio extends IAgregadoRepositorio<PlanoIdentificador, Plano> {
    Plano bloquear(PlanoIdentificador identificador);

    /** A comparação do nome não diferencia maiúsculas de minúsculas. */
    boolean existeAtivoComNomeEDuracao(String nome, int duracaoMeses, Optional<PlanoIdentificador> identificadorIgnorado);
}
