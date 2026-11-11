package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.planodetreino;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoDetalhe;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.SerieDetalhe;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.TreinoDetalhe;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.spi.IPlanoDeTreinoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio.IExercicioRepositorioJpa;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class PlanoDeTreinoAplicacaoRepositorio
        extends AgregadoAplicacaoRepositorioAbstrato<PlanoDeTreinoProjecao, PlanoDeTreinoOrdenacao, PlanoDeTreinoConsulta>
        implements IPlanoDeTreinoAplicacaoRepositorio {
    private final IPlanoDeTreinoRepositorioJpa repositorioJpa;
    private final IExercicioRepositorioJpa exercicios;
    private final ISegurancaServico seguranca;

    public PlanoDeTreinoAplicacaoRepositorio(IPlanoDeTreinoRepositorioJpa repositorioJpa,
            IExercicioRepositorioJpa exercicios, ISegurancaServico seguranca) {
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
        this.exercicios = Objects.requireNonNull(exercicios);
        this.seguranca = Objects.requireNonNull(seguranca);
    }

    @Override
    protected Page<PlanoDeTreinoProjecao> pesquisar(PlanoDeTreinoConsulta consulta, Pageable pageable) {
        var aluno = consulta.getAluno().map(Long::valueOf).orElse(null);
        return repositorioJpa.pesquisar(aluno, consulta.getSituacao().orElse(null), usuarioRestrito(), pageable);
    }

    /** O aluno só enxerga os próprios planos. O personal enxerga todos. */
    private @Nullable String usuarioRestrito() {
        return seguranca.possuiAutorizacao(Autorizacao.USUARIO.name()) ? seguranca.getUsuario() : null;
    }

    @Override
    protected String paraPropriedadeOrdenacao(PlanoDeTreinoOrdenacao ordenacao) {
        return switch (ordenacao) {
        case CRIACAO -> "criacao";
        case NOME -> "nome";
        case ALUNO_NOME -> "a.nome";
        case SITUACAO -> "situacao";
        };
    }

    @Override
    public PlanoDeTreinoDetalhe detalhe(PlanoDeTreinoIdentificador identificador, boolean gerenciavel) {
        Objects.requireNonNull(identificador);
        var codigo = Long.valueOf(identificador.toString());
        var projecao = repositorioJpa.projetar(codigo).orElseThrow(() -> inexistente(identificador));
        var plano = repositorioJpa.findById(codigo).orElseThrow(() -> inexistente(identificador));
        Optional<PlanoDeTreinoIdentificador> anterior = Optional.empty();
        if (plano.getPlanoAnterior() != null) {
            anterior = Optional.of(new PlanoDeTreinoIdentificador(plano.getPlanoAnterior().getIdentificador().toString()));
        }
        return new PlanoDeTreinoDetalhe(projecao, gerenciavel, anterior, paraDetalhes(plano.getTreinos()));
    }

    private List<TreinoDetalhe> paraDetalhes(List<TreinoJpa> treinos) {
        var nomes = nomesDosExercicios();
        var detalhes = new ArrayList<TreinoDetalhe>();
        for (var treino : treinos) {
            var series = new ArrayList<SerieDetalhe>();
            for (var serie : treino.getSeries()) {
                var exercicio = serie.getExercicio().getIdentificador();
                series.add(new SerieDetalhe(new ExercicioIdentificador(exercicio.toString()),
                        Objects.requireNonNull(nomes.get(exercicio)), serie.getQuantidade(), serie.getRepeticoesMinimas(), serie.getRepeticoesMaximas(),
                        serie.getCarga(), serie.getIntervaloSegundos()));
            }
            detalhes.add(new TreinoDetalhe(treino.getNome(), treino.getDia(), treino.getObjetivo(), series));
        }
        return detalhes;
    }

    private Map<Long, String> nomesDosExercicios() {
        var nomes = new HashMap<Long, String>();
        for (var exercicio : exercicios.findAll()) {
            nomes.put(exercicio.getIdentificador(), exercicio.getNome());
        }
        return nomes;
    }

    private static NoSuchElementException inexistente(PlanoDeTreinoIdentificador identificador) {
        return new NoSuchElementException(PlanoDeTreinoServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador));
    }
}
