package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.profissional;

import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.profissional.ProfissionalConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.profissional.ProfissionalOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.profissional.ProfissionalProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.profissional.spi.IProfissionalAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ClassificacaoProfissional;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class ProfissionalAplicacaoRepositorio
        extends AgregadoAplicacaoRepositorioAbstrato<ProfissionalProjecao, ProfissionalOrdenacao, ProfissionalConsulta>
        implements IProfissionalAplicacaoRepositorio {
    private final IProfissionalRepositorioJpa repositorioJpa;

    public ProfissionalAplicacaoRepositorio(IProfissionalRepositorioJpa repositorioJpa) {
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
    }

    /** A busca aceita o CPF com ou sem pontuação. */
    @Override
    protected Page<ProfissionalProjecao> pesquisar(ProfissionalConsulta consulta, Pageable pageable) {
        var busca = consulta.getBusca().map(String::strip).filter(texto -> !texto.isEmpty())
                .map(texto -> texto.matches("[\\d.\\-\\s]+") ? texto.replaceAll("[.\\-\\s]", "") : texto);
        var classificacao = consulta.getClassificacao();
        var professor = classificacao.filter(ClassificacaoProfissional.PROFESSOR::equals).map(item -> true).orElse(null);
        var personal = classificacao.filter(ClassificacaoProfissional.PERSONAL_TRAINER::equals).map(item -> true).orElse(null);
        return repositorioJpa.pesquisar(paraLike(busca), professor, personal, consulta.getSituacao().orElse(null), pageable);
    }

    @Override
    protected String paraPropriedadeOrdenacao(ProfissionalOrdenacao ordenacao) {
        return switch (ordenacao) {
        case NOME -> "nome";
        };
    }
}
