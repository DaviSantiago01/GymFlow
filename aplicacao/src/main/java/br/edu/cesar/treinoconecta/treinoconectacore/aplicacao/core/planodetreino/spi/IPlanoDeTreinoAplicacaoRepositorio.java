package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoDetalhe;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IPlanoDeTreinoAplicacaoRepositorio
        extends IAgregadoAplicacaoRepositorio<PlanoDeTreinoProjecao, PlanoDeTreinoConsulta> {
    PlanoDeTreinoDetalhe detalhe(PlanoDeTreinoIdentificador identificador, boolean gerenciavel);
}
