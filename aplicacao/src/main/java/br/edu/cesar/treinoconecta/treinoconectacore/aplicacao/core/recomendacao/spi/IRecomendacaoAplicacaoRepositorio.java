package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.spi;

import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.RecomendacaoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.RecomendacaoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.VersaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.RecomendacaoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IRecomendacaoAplicacaoRepositorio
        extends IAgregadoAplicacaoRepositorio<RecomendacaoProjecao, RecomendacaoConsulta> {
    RecomendacaoProjecao projetar(RecomendacaoIdentificador identificador);

    /** Versões do plano, da informada até a primeira, seguindo a versão anterior de cada uma. */
    List<VersaoPlano> versoesDoPlano(PlanoDeTreinoIdentificador plano);
}
