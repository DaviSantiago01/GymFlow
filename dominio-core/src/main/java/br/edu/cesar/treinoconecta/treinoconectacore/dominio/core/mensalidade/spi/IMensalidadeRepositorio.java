package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.spi;

import java.time.LocalDate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.Mensalidade;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.MensalidadeIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IMensalidadeRepositorio extends IAgregadoRepositorio<MensalidadeIdentificador, Mensalidade> {
    Mensalidade bloquear(MensalidadeIdentificador identificador);
    boolean existePorAdesaoEPeriodo(AdesaoPlanoIdentificador adesao, LocalDate inicioPeriodo);
}
