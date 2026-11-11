package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.meta;

import java.util.NoSuchElementException;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.MetaConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.MetaOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.MetaProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.spi.IMetaAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.MetaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.MetaServico;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class MetaAplicacaoRepositorio
        extends AgregadoAplicacaoRepositorioAbstrato<MetaProjecao, MetaOrdenacao, MetaConsulta>
        implements IMetaAplicacaoRepositorio {
    private final IMetaRepositorioJpa repositorioJpa;
    private final ISegurancaServico seguranca;

    public MetaAplicacaoRepositorio(IMetaRepositorioJpa repositorioJpa, ISegurancaServico seguranca) {
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
        this.seguranca = Objects.requireNonNull(seguranca);
    }

    @Override
    protected Page<MetaProjecao> pesquisar(MetaConsulta consulta, Pageable pageable) {
        var aluno = consulta.getAluno().map(Long::valueOf).orElse(null);
        return repositorioJpa.pesquisar(aluno, consulta.getSituacao().orElse(null), usuarioRestrito(), pageable);
    }

    /** O aluno só enxerga as próprias metas. O personal enxerga todas. */
    private @Nullable String usuarioRestrito() {
        return seguranca.possuiAutorizacao(Autorizacao.USUARIO.name()) ? seguranca.getUsuario() : null;
    }

    @Override
    protected String paraPropriedadeOrdenacao(MetaOrdenacao ordenacao) {
        return switch (ordenacao) {
        case INICIO -> "inicio";
        case FIM -> "fim";
        case INDICADOR -> "indicador";
        };
    }

    @Override
    public MetaProjecao projetar(MetaIdentificador identificador) {
        Objects.requireNonNull(identificador);
        return repositorioJpa.projetar(Long.valueOf(identificador.toString())).orElseThrow(
                () -> new NoSuchElementException(MetaServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador)));
    }
}
