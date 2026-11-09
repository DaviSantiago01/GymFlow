package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.execucaodeexercicio;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicioServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.spi.IExecucaoDeExercicioRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio.IExercicioRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.planodetreino.IPlanoDeTreinoRepositorioJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class ExecucaoDeExercicioRepositorio extends
        AgregadoRepositorioAbstrato<ExecucaoDeExercicioIdentificador, ExecucaoDeExercicio, Long, ExecucaoDeExercicioJpa>
        implements IExecucaoDeExercicioRepositorio {
    private final IExecucaoDeExercicioRepositorioJpa repositorioJpa;
    private final IAlunoRepositorioJpa alunos;
    private final IPlanoDeTreinoRepositorioJpa planos;
    private final IExercicioRepositorioJpa exercicios;

    public ExecucaoDeExercicioRepositorio(IExecucaoDeExercicioRepositorioJpa repositorioJpa, IAlunoRepositorioJpa alunos,
            IPlanoDeTreinoRepositorioJpa planos, IExercicioRepositorioJpa exercicios) {
        super(repositorioJpa, ExecucaoDeExercicioServico.IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
        this.alunos = Objects.requireNonNull(alunos);
        this.planos = Objects.requireNonNull(planos);
        this.exercicios = Objects.requireNonNull(exercicios);
    }

    @Override
    protected Long paraIdentificadorJpa(ExecucaoDeExercicioIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected ExecucaoDeExercicioJpa paraJpa(ExecucaoDeExercicio agregado) {
        var identificador = agregado.getIdentificador().map(this::paraIdentificadorJpa).orElse(null);
        var aluno = alunos.getReferenceById(Long.valueOf(agregado.getAluno().toString()));
        var plano = planos.getReferenceById(Long.valueOf(agregado.getPlano().toString()));
        var exercicio = exercicios.getReferenceById(Long.valueOf(agregado.getExercicio().toString()));
        return new ExecucaoDeExercicioJpa(identificador, aluno, plano, exercicio, agregado.getDia(), agregado.getData(),
                agregado.getSeries(), agregado.getRepeticoes(), agregado.getCarga());
    }

    @Override
    protected ExecucaoDeExercicio paraAgregado(ExecucaoDeExercicioJpa entidadeJpa) {
        var identificador = new ExecucaoDeExercicioIdentificador(entidadeJpa.getIdentificador().toString());
        var aluno = new AlunoIdentificador(entidadeJpa.getAluno().getIdentificador().toString());
        var plano = new PlanoDeTreinoIdentificador(entidadeJpa.getPlano().getIdentificador().toString());
        var exercicio = new ExercicioIdentificador(entidadeJpa.getExercicio().getIdentificador().toString());
        return new ExecucaoDeExercicio(Optional.of(identificador), aluno, plano, entidadeJpa.getDia(), exercicio,
                entidadeJpa.getData(), entidadeJpa.getSeries(), entidadeJpa.getRepeticoes(), entidadeJpa.getCarga());
    }

    @Override
    public List<ExecucaoDeExercicio> buscarPorAlunoEExercicio(AlunoIdentificador aluno, ExercicioIdentificador exercicio) {
        Objects.requireNonNull(aluno);
        Objects.requireNonNull(exercicio);
        var encontradas = new ArrayList<ExecucaoDeExercicio>();
        var entidades = repositorioJpa.buscarPorAlunoEExercicio(Long.valueOf(aluno.toString()),
                Long.valueOf(exercicio.toString()));
        for (var entidade : entidades) {
            encontradas.add(paraAgregado(entidade));
        }
        return encontradas;
    }
}
