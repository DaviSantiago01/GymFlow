package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.treino.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.treino.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.TreinoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;
public interface ITreinoAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<TreinoProjecao,TreinoConsulta>{TreinoProjecao detalhe(TreinoIdentificador id,String usuario);}
