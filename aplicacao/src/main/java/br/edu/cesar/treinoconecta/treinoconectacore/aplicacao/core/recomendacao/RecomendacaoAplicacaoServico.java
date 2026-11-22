package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.spi.IRecomendacaoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.Recomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.RecomendacaoAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.RecomendacaoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.RecomendacaoServico;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;

public final class RecomendacaoAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<RecomendacaoIdentificador,
        Recomendacao, RecomendacaoAutorizacoes, RecomendacaoProjecao, RecomendacaoConsulta> {
    private final IRecomendacaoAplicacaoRepositorio repositorio;
    private final RecomendacaoServico servico;

    public RecomendacaoAplicacaoServico(IRecomendacaoAplicacaoRepositorio repositorio, RecomendacaoServico servico) {
        super(repositorio, servico);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.servico = Objects.requireNonNull(servico);
    }

    public RecomendacaoProjecao projetar(RecomendacaoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        if (!servico.criarAutorizacoes(Optional.of(identificador)).getBuscar()) {
            throw new NaoAutorizadoExcecao("Ação não autorizada");
        }
        return repositorio.projetar(identificador);
    }

    public List<VersaoPlano> versoesDoPlano(PlanoDeTreinoIdentificador plano) {
        Objects.requireNonNull(plano);
        return repositorio.versoesDoPlano(plano);
    }
}
