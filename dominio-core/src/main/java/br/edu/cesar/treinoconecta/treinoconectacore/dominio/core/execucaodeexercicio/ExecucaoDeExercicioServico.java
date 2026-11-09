package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.spi.IExercicioRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.spi.IExecucaoDeExercicioRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.spi.IPlanoDeTreinoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;

public final class ExecucaoDeExercicioServico extends
        AgregadoServicoAbstrato<ExecucaoDeExercicioIdentificador, ExecucaoDeExercicio, ExecucaoDeExercicioAutorizacoes> {
    public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe uma execução com o identificador %s";
    public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe execução com o identificador %s";
    public static final String PLANO_INEXISTENTE_MODELO = "Não existe plano de treino com o identificador %s";
    public static final String EXERCICIO_INEXISTENTE_MODELO = "Não existe exercício com o identificador %s";
    public static final String SERIES_INVALIDAS = "A quantidade de séries deve ser maior que zero";
    public static final String REPETICOES_INVALIDAS = "As repetições devem ser maiores que zero";
    public static final String CARGA_NEGATIVA = "A carga não pode ser negativa";
    public static final String ALUNO_OBRIGATORIO = "Informe o aluno";
    public static final String DADOS_DE_OUTRO_ALUNO = "O aluno só consulta os próprios dados";
    public static final String SEM_PLANO_ATIVO = "Você não possui um plano de treino ativo";

    private final IExecucaoDeExercicioRepositorio repositorio;
    private final IPlanoDeTreinoRepositorio planos;
    private final IExercicioRepositorio exercicios;
    private final IAlunoRepositorio alunos;
    private final Clock relogio;

    public ExecucaoDeExercicioServico(ISegurancaServico seguranca, IExecucaoDeExercicioRepositorio repositorio,
            IPlanoDeTreinoRepositorio planos, IExercicioRepositorio exercicios, IAlunoRepositorio alunos, Clock relogio) {
        super(seguranca, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.planos = Objects.requireNonNull(planos);
        this.exercicios = Objects.requireNonNull(exercicios);
        this.alunos = Objects.requireNonNull(alunos);
        this.relogio = Objects.requireNonNull(relogio);
    }

    @Override
    public ExecucaoDeExercicioAutorizacoes criarAutorizacoes(Optional<ExecucaoDeExercicioIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean personal = possuiPapel(Autorizacao.PERSONAL);
        boolean aluno = possuiPapel(Autorizacao.USUARIO);
        boolean buscar = personal || aluno;
        if (aluno && identificador.isPresent()) {
            buscar = repositorio.buscarPorIdentificador(identificador.get()).getAluno().equals(alunoAutenticado());
        }
        return new ExecucaoDeExercicioAutorizacoes(buscar, aluno);
    }

    public ExecucaoDeExercicio registrar(PlanoDeTreinoIdentificador plano, DiaSemana dia,
            ExercicioIdentificador exercicio, int series, int repeticoes, BigDecimal carga) {
        Objects.requireNonNull(plano);
        validarAutorizacao(Optional.empty(), ExecucaoDeExercicioAutorizacoes::getRegistrar);
        var aluno = alunoAutenticado();
        var execucao = ExecucaoDeExercicio.registrar(aluno, plano, dia, exercicio, series, repeticoes, carga,
                relogio.instant());
        validarExistencia(planos::existePorIdentificador, plano, PLANO_INEXISTENTE_MODELO);
        validarExistencia(exercicios::existePorIdentificador, exercicio, EXERCICIO_INEXISTENTE_MODELO);
        planos.buscarPorIdentificador(plano).validarExecucao(aluno, dia, exercicio, series, repeticoes);
        return repositorio.incluir(execucao);
    }

    public HistoricoDeCarga consultarHistorico(AlunoIdentificador aluno, ExercicioIdentificador exercicio) {
        Objects.requireNonNull(aluno);
        Objects.requireNonNull(exercicio);
        validarAcessoAoAluno(aluno);
        return new HistoricoDeCarga(repositorio.buscarPorAlunoEExercicio(aluno, exercicio));
    }

    public Optional<SugestaoProgressao> sugerirProgressao(AlunoIdentificador aluno, ExercicioIdentificador exercicio) {
        var historico = consultarHistorico(aluno, exercicio);
        if (!planos.existeAtivoPorAluno(aluno)) {
            return Optional.empty();
        }
        var limite = planos.buscarAtivoPorAluno(aluno).limiteRepeticoes(exercicio);
        if (limite.isEmpty()) {
            return Optional.empty();
        }
        return historico.sugerirProgressao(exercicios.buscarPorIdentificador(exercicio), limite.get());
    }

    public Optional<BigDecimal> maiorCargaValida(AlunoIdentificador aluno, ExercicioIdentificador exercicio) {
        var historico = consultarHistorico(aluno, exercicio);
        return historico.maiorCargaValida(exercicios.buscarPorIdentificador(exercicio));
    }

    public PlanoDeTreino buscarPlanoAtivoDoAlunoAutenticado() {
        validarAutorizacao(Optional.empty(), ExecucaoDeExercicioAutorizacoes::getRegistrar);
        var aluno = alunoAutenticado();
        Validate.validState(planos.existeAtivoPorAluno(aluno), SEM_PLANO_ATIVO);
        return planos.buscarAtivoPorAluno(aluno);
    }

    /** O personal escolhe o aluno. O aluno só pode consultar a si mesmo. */
    public AlunoIdentificador resolverAluno(Optional<AlunoIdentificador> informado) {
        Objects.requireNonNull(informado);
        if (possuiPapel(Autorizacao.PERSONAL)) {
            Validate.isTrue(informado.isPresent(), ALUNO_OBRIGATORIO);
            return informado.get();
        }
        var aluno = alunoAutenticado();
        if (informado.isPresent() && !informado.get().equals(aluno)) {
            throw new NaoAutorizadoExcecao(DADOS_DE_OUTRO_ALUNO);
        }
        return aluno;
    }

    private void validarAcessoAoAluno(AlunoIdentificador aluno) {
        if (!possuiPapel(Autorizacao.PERSONAL) && !aluno.equals(alunoAutenticado())) {
            throw new NaoAutorizadoExcecao(DADOS_DE_OUTRO_ALUNO);
        }
    }

    private boolean possuiPapel(Autorizacao papel) {
        return getSegurancaServico().possuiAutorizacao(papel.name());
    }

    private AlunoIdentificador alunoAutenticado() {
        return alunos.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow();
    }
}
