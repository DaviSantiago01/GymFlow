package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.vinculopersonal;

import java.util.NoSuchElementException;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.vinculopersonal.VinculoPersonalAlunoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.vinculopersonal.VinculoPersonalAlunoOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.vinculopersonal.VinculoPersonalAlunoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.vinculopersonal.spi.IVinculoPersonalAlunoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAlunoServico;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class VinculoPersonalAlunoAplicacaoRepositorio extends AgregadoAplicacaoRepositorioAbstrato<
        VinculoPersonalAlunoProjecao, VinculoPersonalAlunoOrdenacao, VinculoPersonalAlunoConsulta>
        implements IVinculoPersonalAlunoAplicacaoRepositorio {
    private final IVinculoPersonalAlunoRepositorioJpa repositorioJpa;

    public VinculoPersonalAlunoAplicacaoRepositorio(IVinculoPersonalAlunoRepositorioJpa repositorioJpa) {
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
    }

    @Override
    protected Page<VinculoPersonalAlunoProjecao> pesquisar(VinculoPersonalAlunoConsulta consulta, Pageable pageable) {
        return repositorioJpa.pesquisar(consulta.getProfissional().map(Long::valueOf).orElse(null),
                consulta.getAluno().map(Long::valueOf).orElse(null), consulta.getSituacao().orElse(null), pageable);
    }

    @Override
    protected String paraPropriedadeOrdenacao(VinculoPersonalAlunoOrdenacao ordenacao) {
        return switch (ordenacao) {
        case INICIO -> "inicio";
        case ALUNO -> "aluno.nome";
        };
    }

    @Override
    public VinculoPersonalAlunoProjecao projetar(VinculoPersonalAlunoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        return repositorioJpa.projetar(Long.valueOf(identificador.toString())).orElseThrow(() -> new NoSuchElementException(
                VinculoPersonalAlunoServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador)));
    }
}
