package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.spi.IExecucaoDeExercicioAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoDetalhe;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicioAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicioServico;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;

public final class ExecucaoDeExercicioAplicacaoServico extends
        AgregadoAplicacaoServicoAbstrato<ExecucaoDeExercicioIdentificador, ExecucaoDeExercicio, ExecucaoDeExercicioAutorizacoes, ExecucaoDeExercicioProjecao, ExecucaoDeExercicioConsulta> {
    private final IExecucaoDeExercicioAplicacaoRepositorio repositorio;
    private final ExecucaoDeExercicioServico servico;
    private final PlanoDeTreinoAplicacaoServico planos;

    public ExecucaoDeExercicioAplicacaoServico(IExecucaoDeExercicioAplicacaoRepositorio repositorio,
            ExecucaoDeExercicioServico servico, PlanoDeTreinoAplicacaoServico planos) {
        super(repositorio, servico);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.servico = Objects.requireNonNull(servico);
        this.planos = Objects.requireNonNull(planos);
    }

    public PlanoDeTreinoDetalhe planoAtivo() {
        var plano = servico.buscarPlanoAtivoDoAlunoAutenticado();
        return planos.detalhe(plano.getIdentificador().orElseThrow());
    }

    public ExecucaoRegistrada resultado(ExecucaoDeExercicio execucao) {
        Objects.requireNonNull(execucao);
        var projecao = repositorio.projetar(execucao.getIdentificador().orElseThrow());
        return new ExecucaoRegistrada(projecao, servico.sugerirProgressao(execucao.getAluno(), execucao.getExercicio()));
    }

    public EvolucaoExercicio evolucao(Optional<AlunoIdentificador> alunoInformado,
            Optional<ExercicioIdentificador> exercicioInformado) {
        Objects.requireNonNull(exercicioInformado);
        var aluno = servico.resolverAluno(alunoInformado);
        var exercicios = repositorio.exerciciosExecutados(aluno);
        var exercicio = escolherExercicio(exercicioInformado, exercicios);
        if (exercicio.isEmpty()) {
            return new EvolucaoExercicio(aluno, exercicio, exercicios, List.of(), Optional.empty(), Optional.empty());
        }
        return new EvolucaoExercicio(aluno, exercicio, exercicios,
                repositorio.listarCronologicamente(aluno, exercicio.get()),
                servico.maiorCargaValida(aluno, exercicio.get()), servico.sugerirProgressao(aluno, exercicio.get()));
    }

    private static Optional<ExercicioIdentificador> escolherExercicio(Optional<ExercicioIdentificador> informado,
            List<ExercicioExecutado> exercicios) {
        if (informado.isPresent() || exercicios.isEmpty()) {
            return informado;
        }
        return Optional.of(exercicios.get(0).identificador());
    }
}
