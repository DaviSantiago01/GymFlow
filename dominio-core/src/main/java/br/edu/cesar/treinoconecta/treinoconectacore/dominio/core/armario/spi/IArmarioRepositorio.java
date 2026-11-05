package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.*;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;
public interface IArmarioRepositorio extends IAgregadoRepositorio<ArmarioCodigo, Armario> {
    Armario bloquear(ArmarioCodigo codigo);
}
