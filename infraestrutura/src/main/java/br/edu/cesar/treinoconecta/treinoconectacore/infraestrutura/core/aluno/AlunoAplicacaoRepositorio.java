package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.spi.IAlunoAplicacaoRepositorio;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class AlunoAplicacaoRepositorio
        extends AgregadoAplicacaoRepositorioAbstrato<AlunoProjecao, AlunoOrdenacao, AlunoConsulta>
        implements IAlunoAplicacaoRepositorio {
    private final IAlunoConsultaJpa repositorioJpa;

    public AlunoAplicacaoRepositorio(IAlunoConsultaJpa repositorioJpa) {
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
    }

    @Override
    protected Page<AlunoProjecao> pesquisar(AlunoConsulta consulta, Pageable pageable) {
        return repositorioJpa.pesquisar(paraLike(consulta.getNome()), pageable);
    }

    @Override
    protected String paraPropriedadeOrdenacao(AlunoOrdenacao ordenacao) {
        return switch (ordenacao) {
        case NOME -> "nome";
        case USUARIO -> "usuario";
        };
    }
}
