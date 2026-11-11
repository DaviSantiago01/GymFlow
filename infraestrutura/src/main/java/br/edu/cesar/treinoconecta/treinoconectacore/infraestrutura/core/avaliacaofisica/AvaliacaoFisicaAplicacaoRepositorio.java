package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaofisica;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.AvaliacaoFisicaConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.AvaliacaoFisicaOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.AvaliacaoFisicaProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.spi.IAvaliacaoFisicaAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaServico;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class AvaliacaoFisicaAplicacaoRepositorio extends
        AgregadoAplicacaoRepositorioAbstrato<AvaliacaoFisicaProjecao, AvaliacaoFisicaOrdenacao, AvaliacaoFisicaConsulta>
        implements IAvaliacaoFisicaAplicacaoRepositorio {
    private final IAvaliacaoFisicaRepositorioJpa repositorioJpa;
    private final ISegurancaServico seguranca;

    public AvaliacaoFisicaAplicacaoRepositorio(IAvaliacaoFisicaRepositorioJpa repositorioJpa, ISegurancaServico seguranca) {
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
        this.seguranca = Objects.requireNonNull(seguranca);
    }

    @Override
    protected Page<AvaliacaoFisicaProjecao> pesquisar(AvaliacaoFisicaConsulta consulta, Pageable pageable) {
        var aluno = consulta.getAluno().map(Long::valueOf).orElse(null);
        return repositorioJpa.pesquisar(aluno, consulta.getSituacao().orElse(null), usuarioRestrito(), pageable);
    }

    /** O aluno só enxerga as próprias avaliações. O personal enxerga todas. */
    private @Nullable String usuarioRestrito() {
        return seguranca.possuiAutorizacao(Autorizacao.USUARIO.name()) ? seguranca.getUsuario() : null;
    }

    @Override
    protected String paraPropriedadeOrdenacao(AvaliacaoFisicaOrdenacao ordenacao) {
        return switch (ordenacao) {
        case DATA -> "data";
        case ALUNO_NOME -> "a.nome";
        };
    }

    @Override
    public AvaliacaoFisicaProjecao projetar(AvaliacaoFisicaIdentificador identificador) {
        Objects.requireNonNull(identificador);
        return repositorioJpa.projetar(Long.valueOf(identificador.toString()))
                .orElseThrow(() -> new NoSuchElementException(
                        AvaliacaoFisicaServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador)));
    }

    @Override
    public List<AvaliacaoFisicaProjecao> listarValidas(AlunoIdentificador aluno) {
        Objects.requireNonNull(aluno);
        return repositorioJpa.listarValidas(Long.valueOf(aluno.toString()));
    }
}
