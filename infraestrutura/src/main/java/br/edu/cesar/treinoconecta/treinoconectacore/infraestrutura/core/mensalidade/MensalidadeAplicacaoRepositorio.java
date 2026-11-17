package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.mensalidade;

import java.time.Clock;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.spi.IMensalidadeAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.MensalidadeIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.MensalidadeServico;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class MensalidadeAplicacaoRepositorio
        extends AgregadoAplicacaoRepositorioAbstrato<MensalidadeProjecao, MensalidadeOrdenacao, MensalidadeConsulta>
        implements IMensalidadeAplicacaoRepositorio {
    private final IMensalidadeRepositorioJpa repositorioJpa;
    private final ISegurancaServico seguranca;
    private final Clock relogio;

    public MensalidadeAplicacaoRepositorio(IMensalidadeRepositorioJpa repositorioJpa, ISegurancaServico seguranca,
            Clock relogio) {
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
        this.seguranca = Objects.requireNonNull(seguranca);
        this.relogio = Objects.requireNonNull(relogio);
    }

    @Override
    protected Page<MensalidadeProjecao> pesquisar(MensalidadeConsulta consulta, Pageable pageable) {
        var aluno = consulta.getAluno().map(Long::valueOf).orElse(null);
        var adesao = consulta.getAdesao().map(Long::valueOf).orElse(null);
        var filtro = consulta.getSituacao().map(Enum::name).orElse(null);
        var hoje = relogio.instant().atZone(ParametrosAcademia.FUSO).toLocalDate();
        return repositorioJpa.pesquisar(aluno, adesao, usuarioRestrito(), filtro, hoje, pageable);
    }

    /** O aluno só enxerga as próprias mensalidades. O administrador enxerga todas. */
    private @Nullable String usuarioRestrito() {
        return seguranca.possuiAutorizacao(Autorizacao.USUARIO.name()) ? seguranca.getUsuario() : null;
    }

    @Override
    protected String paraPropriedadeOrdenacao(MensalidadeOrdenacao ordenacao) {
        return switch (ordenacao) {
        case VENCIMENTO -> "vencimento";
        case ALUNO -> "aluno.nome";
        };
    }

    @Override
    public MensalidadeProjecao projetar(MensalidadeIdentificador identificador) {
        Objects.requireNonNull(identificador);
        return repositorioJpa.projetar(Long.valueOf(identificador.toString())).orElseThrow(() -> new NoSuchElementException(
                MensalidadeServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador)));
    }
}
