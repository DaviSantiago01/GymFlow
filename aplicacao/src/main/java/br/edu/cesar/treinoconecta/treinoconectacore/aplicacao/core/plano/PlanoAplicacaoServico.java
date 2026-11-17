package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.spi.IPlanoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.Plano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.SituacaoPlano;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class PlanoAplicacaoServico
        extends AgregadoAplicacaoServicoAbstrato<PlanoIdentificador, Plano, PlanoAutorizacoes, PlanoProjecao, PlanoConsulta> {
    private final IPlanoAplicacaoRepositorio repositorio;
    private final PlanoServico servico;

    public PlanoAplicacaoServico(IPlanoAplicacaoRepositorio repositorio, PlanoServico servico) {
        super(repositorio, servico);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.servico = Objects.requireNonNull(servico);
    }

    public PlanoProjecao projetar(PlanoIdentificador identificador) {
        servico.buscar(identificador);
        return repositorio.projetar(identificador);
    }

    /** Planos que podem receber novas adesões, para os formulários de adesão e troca. */
    public List<PlanoProjecao> listarAtivos() {
        var consulta = new PlanoConsulta(Optional.of(PlanoOrdenacao.NOME), Optional.empty(), Optional.empty(),
                Optional.of(Consulta.TAMANHO_PAGINA_MAXIMO), Optional.empty(), Optional.of(SituacaoPlano.ATIVO));
        return repositorio.pesquisar(consulta).getProjecoes();
    }
}
