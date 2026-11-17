package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.adesaoplano;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano.AdesaoPlanoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano.AdesaoPlanoOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano.AdesaoPlanoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano.spi.IAdesaoPlanoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoServico;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class AdesaoPlanoAplicacaoRepositorio
        extends AgregadoAplicacaoRepositorioAbstrato<AdesaoPlanoProjecao, AdesaoPlanoOrdenacao, AdesaoPlanoConsulta>
        implements IAdesaoPlanoAplicacaoRepositorio {
    private final IAdesaoPlanoRepositorioJpa repositorioJpa;
    private final ISegurancaServico seguranca;

    public AdesaoPlanoAplicacaoRepositorio(IAdesaoPlanoRepositorioJpa repositorioJpa, ISegurancaServico seguranca) {
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
        this.seguranca = Objects.requireNonNull(seguranca);
    }

    @Override
    protected Page<AdesaoPlanoProjecao> pesquisar(AdesaoPlanoConsulta consulta, Pageable pageable) {
        var aluno = consulta.getAluno().map(Long::valueOf).orElse(null);
        return repositorioJpa.pesquisar(aluno, consulta.getSituacao().orElse(null), usuarioRestrito(), pageable);
    }

    /** O aluno só enxerga as próprias adesões. O administrador enxerga todas. */
    private @Nullable String usuarioRestrito() {
        return seguranca.possuiAutorizacao(Autorizacao.USUARIO.name()) ? seguranca.getUsuario() : null;
    }

    @Override
    protected String paraPropriedadeOrdenacao(AdesaoPlanoOrdenacao ordenacao) {
        return switch (ordenacao) {
        case INICIO -> "inicio";
        case ALUNO -> "aluno.nome";
        };
    }

    @Override
    public AdesaoPlanoProjecao projetar(AdesaoPlanoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        return repositorioJpa.projetar(Long.valueOf(identificador.toString())).orElseThrow(() -> new NoSuchElementException(
                AdesaoPlanoServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador)));
    }

    @Override
    public Optional<AdesaoPlanoProjecao> projetarAtivaDoUsuario(String usuario) {
        return repositorioJpa.projetarAtivaDoUsuario(Objects.requireNonNull(usuario));
    }
}
