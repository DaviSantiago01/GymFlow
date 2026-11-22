package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.metaacademia;

import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.metaacademia.MetaAcademiaConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.metaacademia.MetaAcademiaOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.metaacademia.MetaAcademiaProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.metaacademia.spi.IMetaAcademiaAplicacaoRepositorio;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class MetaAcademiaAplicacaoRepositorio
        extends AgregadoAplicacaoRepositorioAbstrato<MetaAcademiaProjecao, MetaAcademiaOrdenacao, MetaAcademiaConsulta>
        implements IMetaAcademiaAplicacaoRepositorio {
    private final IMetaAcademiaRepositorioJpa repositorioJpa;

    public MetaAcademiaAplicacaoRepositorio(IMetaAcademiaRepositorioJpa repositorioJpa) {
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
    }

    @Override
    protected Page<MetaAcademiaProjecao> pesquisar(MetaAcademiaConsulta consulta, Pageable pageable) {
        return repositorioJpa.pesquisar(consulta.getSituacao().orElse(null), pageable);
    }

    @Override
    protected String paraPropriedadeOrdenacao(MetaAcademiaOrdenacao ordenacao) {
        return switch (ordenacao) {
        case INICIO -> "inicio";
        case INDICADOR -> "indicador";
        };
    }
}
