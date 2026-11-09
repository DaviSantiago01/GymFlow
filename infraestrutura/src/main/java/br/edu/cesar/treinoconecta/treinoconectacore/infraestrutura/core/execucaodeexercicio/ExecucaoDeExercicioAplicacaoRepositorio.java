package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.execucaodeexercicio;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExecucaoDeExercicioConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExecucaoDeExercicioOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExecucaoDeExercicioProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExercicioExecutado;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.spi.IExecucaoDeExercicioAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicioServico;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class ExecucaoDeExercicioAplicacaoRepositorio extends
        AgregadoAplicacaoRepositorioAbstrato<ExecucaoDeExercicioProjecao, ExecucaoDeExercicioOrdenacao, ExecucaoDeExercicioConsulta>
        implements IExecucaoDeExercicioAplicacaoRepositorio {
    private final IExecucaoDeExercicioRepositorioJpa repositorioJpa;
    private final ISegurancaServico seguranca;

    public ExecucaoDeExercicioAplicacaoRepositorio(IExecucaoDeExercicioRepositorioJpa repositorioJpa,
            ISegurancaServico seguranca) {
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
        this.seguranca = Objects.requireNonNull(seguranca);
    }

    @Override
    protected Page<ExecucaoDeExercicioProjecao> pesquisar(ExecucaoDeExercicioConsulta consulta, Pageable pageable) {
        var aluno = consulta.getAluno().map(Long::valueOf).orElse(null);
        var exercicio = consulta.getExercicio().map(Long::valueOf).orElse(null);
        return repositorioJpa.pesquisar(aluno, exercicio, usuarioRestrito(), pageable);
    }

    /** O aluno só enxerga as próprias execuções. O personal enxerga todas. */
    private @Nullable String usuarioRestrito() {
        return seguranca.possuiAutorizacao(Autorizacao.USUARIO.name()) ? seguranca.getUsuario() : null;
    }

    @Override
    protected String paraPropriedadeOrdenacao(ExecucaoDeExercicioOrdenacao ordenacao) {
        return switch (ordenacao) {
        case DATA -> "data";
        case EXERCICIO_NOME -> "x.nome";
        case CARGA -> "carga";
        };
    }

    @Override
    public ExecucaoDeExercicioProjecao projetar(ExecucaoDeExercicioIdentificador identificador) {
        Objects.requireNonNull(identificador);
        return repositorioJpa.projetar(Long.valueOf(identificador.toString()))
                .orElseThrow(() -> new NoSuchElementException(
                        ExecucaoDeExercicioServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador)));
    }

    @Override
    public List<ExecucaoDeExercicioProjecao> listarCronologicamente(AlunoIdentificador aluno,
            ExercicioIdentificador exercicio) {
        Objects.requireNonNull(aluno);
        Objects.requireNonNull(exercicio);
        return repositorioJpa.listarCronologicamente(Long.valueOf(aluno.toString()), Long.valueOf(exercicio.toString()));
    }

    @Override
    public List<ExercicioExecutado> exerciciosExecutados(AlunoIdentificador aluno) {
        Objects.requireNonNull(aluno);
        return repositorioJpa.exerciciosExecutados(Long.valueOf(aluno.toString()));
    }
}
