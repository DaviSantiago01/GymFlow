package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.Relatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.RelatorioIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IRelatorioRepositorio extends IAgregadoRepositorio<RelatorioIdentificador, Relatorio> {
    Relatorio bloquear(RelatorioIdentificador identificador);
}
