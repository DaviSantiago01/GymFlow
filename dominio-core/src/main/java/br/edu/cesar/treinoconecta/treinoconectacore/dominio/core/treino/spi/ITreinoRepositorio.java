package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.*;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;
public interface ITreinoRepositorio extends IAgregadoRepositorio<TreinoIdentificador,Treino> { Treino bloquear(TreinoIdentificador id); }
