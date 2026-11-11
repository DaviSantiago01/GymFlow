package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.planodetreino;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.Serie;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.Treino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.spi.IPlanoDeTreinoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio.IExercicioRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.IPersonalRepositorioJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class PlanoDeTreinoRepositorio
        extends AgregadoRepositorioAbstrato<PlanoDeTreinoIdentificador, PlanoDeTreino, Long, PlanoDeTreinoJpa>
        implements IPlanoDeTreinoRepositorio {
    private final IPlanoDeTreinoRepositorioJpa repositorioJpa;
    private final IAlunoRepositorioJpa alunos;
    private final IPersonalRepositorioJpa personais;
    private final IExercicioRepositorioJpa exercicios;

    public PlanoDeTreinoRepositorio(IPlanoDeTreinoRepositorioJpa repositorioJpa, IAlunoRepositorioJpa alunos,
            IPersonalRepositorioJpa personais, IExercicioRepositorioJpa exercicios) {
        super(repositorioJpa, PlanoDeTreinoServico.IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
        this.alunos = Objects.requireNonNull(alunos);
        this.personais = Objects.requireNonNull(personais);
        this.exercicios = Objects.requireNonNull(exercicios);
    }

    @Override
    protected Long paraIdentificadorJpa(PlanoDeTreinoIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected PlanoDeTreinoJpa paraJpa(PlanoDeTreino agregado) {
        var identificador = agregado.getIdentificador().map(this::paraIdentificadorJpa).orElse(null);
        var aluno = alunos.getReferenceById(Long.valueOf(agregado.getAluno().toString()));
        var personal = personais.getReferenceById(Long.valueOf(agregado.getPersonal().toString()));
        var anterior = agregado.getPlanoAnterior().map(this::paraIdentificadorJpa).map(repositorioJpa::getReferenceById)
                .orElse(null);
        return new PlanoDeTreinoJpa(identificador, aluno, personal, agregado.getNome(), agregado.getFrequenciaSemanal(),
                agregado.getSituacao(), agregado.getVersao(), anterior, agregado.getCriacao(),
                agregado.getEncerramento().orElse(null), paraTreinosJpa(agregado.getTreinos()));
    }

    private List<TreinoJpa> paraTreinosJpa(List<Treino> treinos) {
        var treinosJpa = new ArrayList<TreinoJpa>();
        for (var treino : treinos) {
            var seriesJpa = new ArrayList<SerieJpa>();
            for (var serie : treino.series()) {
                var exercicio = exercicios.getReferenceById(Long.valueOf(serie.exercicio().toString()));
                seriesJpa.add(new SerieJpa(exercicio, serie.quantidade(), serie.repeticoesMinimas(),
                        serie.repeticoesMaximas(), serie.carga(), serie.intervaloSegundos()));
            }
            treinosJpa.add(new TreinoJpa(treino.nome(), treino.dia(), treino.objetivo(), seriesJpa));
        }
        return treinosJpa;
    }

    @Override
    protected PlanoDeTreino paraAgregado(PlanoDeTreinoJpa entidadeJpa) {
        var identificador = new PlanoDeTreinoIdentificador(entidadeJpa.getIdentificador().toString());
        var aluno = new AlunoIdentificador(entidadeJpa.getAluno().getIdentificador().toString());
        var personal = new PersonalIdentificador(entidadeJpa.getPersonal().getIdentificador().toString());
        Optional<PlanoDeTreinoIdentificador> anterior = Optional.empty();
        if (entidadeJpa.getPlanoAnterior() != null) {
            anterior = Optional.of(new PlanoDeTreinoIdentificador(entidadeJpa.getPlanoAnterior().getIdentificador().toString()));
        }
        return new PlanoDeTreino(Optional.of(identificador), aluno, personal, entidadeJpa.getNome(),
                entidadeJpa.getFrequenciaSemanal(), paraTreinos(entidadeJpa.getTreinos()), entidadeJpa.getSituacao(),
                entidadeJpa.getVersao(), anterior, entidadeJpa.getCriacao(),
                Optional.ofNullable(entidadeJpa.getEncerramento()));
    }

    private List<Treino> paraTreinos(List<TreinoJpa> treinosJpa) {
        var treinos = new ArrayList<Treino>();
        for (var treinoJpa : treinosJpa) {
            var series = new ArrayList<Serie>();
            for (var serieJpa : treinoJpa.getSeries()) {
                var exercicio = new ExercicioIdentificador(serieJpa.getExercicio().getIdentificador().toString());
                series.add(new Serie(exercicio, serieJpa.getQuantidade(), serieJpa.getRepeticoesMinimas(),
                        serieJpa.getRepeticoesMaximas(), serieJpa.getCarga(), serieJpa.getIntervaloSegundos()));
            }
            treinos.add(new Treino(treinoJpa.getNome(), treinoJpa.getDia(), treinoJpa.getObjetivo(), series));
        }
        return treinos;
    }

    /** Grava na hora para liberar o índice de plano ativo antes de ativar outra versão. */
    @Override
    public void alterar(PlanoDeTreino agregado) {
        Objects.requireNonNull(agregado);
        var identificador = paraIdentificadorJpa(agregado.getIdentificador().orElseThrow());
        var plano = repositorioJpa.findById(identificador).orElseThrow(() -> inexistente(identificador));
        plano.atualizarSituacao(agregado.getSituacao(), agregado.getEncerramento().orElse(null));
        repositorioJpa.saveAndFlush(plano);
    }

    @Override
    public PlanoDeTreino bloquear(PlanoDeTreinoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        var codigo = paraIdentificadorJpa(identificador);
        return paraAgregado(repositorioJpa.bloquear(codigo).orElseThrow(() -> inexistente(codigo)));
    }

    @Override
    public boolean existeAtivoPorAluno(AlunoIdentificador aluno) {
        Objects.requireNonNull(aluno);
        return repositorioJpa.existeAtivoPorAluno(Long.valueOf(aluno.toString()));
    }

    @Override
    public PlanoDeTreino buscarAtivoPorAluno(AlunoIdentificador aluno) {
        Objects.requireNonNull(aluno);
        var plano = repositorioJpa.buscarAtivoPorAluno(Long.valueOf(aluno.toString()))
                .orElseThrow(() -> new NoSuchElementException(PlanoDeTreinoServico.ALUNO_SEM_PLANO_ATIVO));
        return paraAgregado(plano);
    }

    private static NoSuchElementException inexistente(Long identificador) {
        return new NoSuchElementException(PlanoDeTreinoServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador));
    }
}
