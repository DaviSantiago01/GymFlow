package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.plano;

import java.util.NoSuchElementException;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.PlanoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.PlanoOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.PlanoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.spi.IPlanoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoServico;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class PlanoAplicacaoRepositorio
        extends AgregadoAplicacaoRepositorioAbstrato<PlanoProjecao, PlanoOrdenacao, PlanoConsulta>
        implements IPlanoAplicacaoRepositorio {
    private final IPlanoRepositorioJpa repositorioJpa;

    public PlanoAplicacaoRepositorio(IPlanoRepositorioJpa repositorioJpa) {
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
    }

    @Override
    protected Page<PlanoProjecao> pesquisar(PlanoConsulta consulta, Pageable pageable) {
        return repositorioJpa.pesquisar(paraLike(consulta.getNome()), consulta.getSituacao().orElse(null), pageable);
    }

    @Override
    protected String paraPropriedadeOrdenacao(PlanoOrdenacao ordenacao) {
        return switch (ordenacao) {
        case NOME -> "nome";
        case VALOR -> "valor";
        case DURACAO -> "duracaoMeses";
        };
    }

    @Override
    public PlanoProjecao projetar(PlanoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        return repositorioJpa.projetar(Long.valueOf(identificador.toString())).orElseThrow(
                () -> new NoSuchElementException(PlanoServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador)));
    }
}
