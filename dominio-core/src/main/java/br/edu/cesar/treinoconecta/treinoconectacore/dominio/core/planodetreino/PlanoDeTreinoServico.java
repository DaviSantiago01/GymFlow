package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.spi.IExercicioRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.spi.IPersonalRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.spi.IPlanoDeTreinoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

public final class PlanoDeTreinoServico
        extends AgregadoServicoAbstrato<PlanoDeTreinoIdentificador, PlanoDeTreino, PlanoDeTreinoAutorizacoes> {
    public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe um plano de treino com o identificador %s";
    public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe plano de treino com o identificador %s";
    public static final String ALUNO_INEXISTENTE_MODELO = "Não existe aluno com o identificador %s";
    public static final String EXERCICIO_INEXISTENTE_MODELO = "Não existe exercício com o identificador %s";
    public static final String NOME_INVALIDO = "Informe um nome de plano com até 100 caracteres";
    public static final String NOME_TREINO_INVALIDO = "Informe um nome de treino com até 60 caracteres";
    public static final String FREQUENCIA_INVALIDA = "A frequência semanal deve estar entre 1 e 7 dias";
    public static final String VERSAO_INVALIDA = "A versão do plano deve ser maior que zero";
    public static final String DIA_REPETIDO_MODELO = "O plano já possui um treino em %s";
    public static final String EXERCICIO_REPETIDO_MODELO = "O exercício %s já faz parte do treino %s";
    public static final String SEM_EXERCICIO_REPETIDO_MODELO = "O treino %s não possui exercício repetido";
    public static final String SERIE_INEXISTENTE_MODELO = "O exercício %s não faz parte do treino %s";
    public static final String EXERCICIO_FORA_DO_PLANO_MODELO = "O exercício %s não faz parte do plano";
    public static final String SERIES_INVALIDAS = "A quantidade de séries deve ser maior que zero";
    public static final String REPETICOES_INVALIDAS = "As repetições devem ser maiores que zero";
    public static final String FAIXA_REPETICOES_INVALIDA = "O limite superior de repetições não pode ser menor que o inferior";
    public static final String CARGA_NEGATIVA = "A carga não pode ser negativa";
    public static final String INTERVALO_FORA_DOS_LIMITES_MODELO = "O intervalo deve estar entre %d e %d segundos";
    public static final String SERIES_FORA_DA_FAIXA_MODELO = "O objetivo %s exige de %d a %d séries";
    public static final String REPETICOES_FORA_DA_FAIXA_MODELO = "O objetivo %s exige de %d a %d repetições";
    public static final String PLANO_VAZIO = "Um plano vazio não pode ser ativado: cadastre pelo menos um treino";
    public static final String TREINO_SEM_EXERCICIOS_MODELO = "O treino %s precisa de exercícios e séries para o plano ser ativado";
    public static final String FREQUENCIA_DIVERGENTE_MODELO = "A frequência semanal declarada (%d) difere dos dias de treino cadastrados (%d)";
    public static final String PLANO_ATIVO_EXISTENTE = "O aluno já possui um plano ativo. Encerre ou substitua o plano atual";
    public static final String ALUNO_SEM_PLANO_ATIVO = "O aluno não possui plano ativo para substituir";
    public static final String SOMENTE_RASCUNHO_ATIVA = "Somente um plano em rascunho pode ser ativado";
    public static final String PLANO_JA_FINALIZADO = "Este plano já foi encerrado ou substituído";
    public static final String PLANO_NAO_ALTERAVEL = "Um plano encerrado ou substituído não pode ser alterado";
    public static final String PLANO_DE_OUTRO_PERSONAL = "Este plano está sob a responsabilidade de outro personal";
    public static final String PLANO_DE_OUTRO_ALUNO = "Este plano pertence a outro aluno";
    public static final String PLANO_ENCERRADO = "Não é possível registrar execução em um plano encerrado";
    public static final String PLANO_NAO_ATIVO = "Somente o plano ativo aceita novas execuções";
    public static final String EXERCICIO_FORA_DO_TREINO_MODELO = "O exercício %s não pertence ao treino de %s do plano";
    public static final String SERIES_ACIMA_DO_PLANO_MODELO = "O plano prevê no máximo %d séries para este exercício";
    public static final String REPETICOES_ACIMA_DO_PLANO_MODELO = "O plano prevê no máximo %d repetições para este exercício";

    private final IPlanoDeTreinoRepositorio repositorio;
    private final IAlunoRepositorio alunos;
    private final IPersonalRepositorio personais;
    private final IExercicioRepositorio exercicios;
    private final Clock relogio;

    public PlanoDeTreinoServico(ISegurancaServico seguranca, IPlanoDeTreinoRepositorio repositorio,
            IAlunoRepositorio alunos, IPersonalRepositorio personais, IExercicioRepositorio exercicios, Clock relogio) {
        super(seguranca, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.alunos = Objects.requireNonNull(alunos);
        this.personais = Objects.requireNonNull(personais);
        this.exercicios = Objects.requireNonNull(exercicios);
        this.relogio = Objects.requireNonNull(relogio);
    }

    @Override
    public PlanoDeTreinoAutorizacoes criarAutorizacoes(Optional<PlanoDeTreinoIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean personal = getSegurancaServico().possuiAutorizacao(Autorizacao.PERSONAL.name());
        boolean aluno = getSegurancaServico().possuiAutorizacao(Autorizacao.USUARIO.name());
        boolean buscar = personal || aluno;
        if (aluno && identificador.isPresent()) {
            buscar = repositorio.buscarPorIdentificador(identificador.get()).getAluno().equals(alunoAutenticado());
        }
        return new PlanoDeTreinoAutorizacoes(buscar, personal, personal, personal, personal, personal);
    }

    public PlanoDeTreino criar(AlunoIdentificador aluno, String nome, int frequenciaSemanal, List<Treino> treinos) {
        Objects.requireNonNull(aluno);
        validarAutorizacao(Optional.empty(), PlanoDeTreinoAutorizacoes::getCriar);
        var plano = PlanoDeTreino.criar(aluno, personalAutenticado(), nome, frequenciaSemanal, treinos, relogio.instant());
        validarExistencia(alunos::existePorIdentificador, aluno, ALUNO_INEXISTENTE_MODELO);
        validarExercicios(treinos);
        return repositorio.incluir(plano);
    }

    public PlanoDeTreino ativar(PlanoDeTreinoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), PlanoDeTreinoAutorizacoes::getAtivar);
        validarExistencia(identificador);
        bloquearAlunoDoPlano(identificador);
        var ativado = repositorio.bloquear(identificador).ativar(personalAutenticado());
        Validate.validState(!repositorio.existeAtivoPorAluno(ativado.getAluno()), PLANO_ATIVO_EXISTENTE);
        repositorio.alterar(ativado);
        return ativado;
    }

    /** Ativa o rascunho e deixa o plano ativo atual como substituído. */
    public PlanoDeTreino substituir(PlanoDeTreinoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), PlanoDeTreinoAutorizacoes::getSubstituir);
        validarExistencia(identificador);
        bloquearAlunoDoPlano(identificador);
        var personal = personalAutenticado();
        var ativado = repositorio.bloquear(identificador).ativar(personal);
        Validate.validState(repositorio.existeAtivoPorAluno(ativado.getAluno()), ALUNO_SEM_PLANO_ATIVO);
        var atual = repositorio.buscarAtivoPorAluno(ativado.getAluno());
        repositorio.alterar(atual.substituir(personal, relogio.instant()));
        repositorio.alterar(ativado);
        return ativado;
    }

    public PlanoDeTreino revisar(PlanoDeTreinoIdentificador identificador, String nome, int frequenciaSemanal,
            List<Treino> treinos) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), PlanoDeTreinoAutorizacoes::getRevisar);
        validarExistencia(identificador);
        validarExercicios(treinos);
        bloquearAlunoDoPlano(identificador);
        var personal = personalAutenticado();
        var atual = repositorio.bloquear(identificador);
        var agora = relogio.instant();
        var revisado = atual.revisar(personal, nome, frequenciaSemanal, treinos, agora);
        repositorio.alterar(atual.substituir(personal, agora));
        return repositorio.incluir(revisado);
    }

    public PlanoDeTreino encerrar(PlanoDeTreinoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), PlanoDeTreinoAutorizacoes::getEncerrar);
        validarExistencia(identificador);
        var encerrado = repositorio.bloquear(identificador).encerrar(personalAutenticado(), relogio.instant());
        repositorio.alterar(encerrado);
        return encerrado;
    }

    public boolean podeGerenciar(PlanoDeTreino plano) {
        Objects.requireNonNull(plano);
        if (!getSegurancaServico().possuiAutorizacao(Autorizacao.PERSONAL.name())) {
            return false;
        }
        return plano.getPersonal().equals(personalAutenticado());
    }

    /** Valida os exercícios informados e, pela RN003, informa o nome do exercício repetido. */
    private void validarExercicios(List<Treino> treinos) {
        for (var treino : treinos) {
            for (var serie : treino.series()) {
                validarExistencia(exercicios::existePorIdentificador, serie.exercicio(), EXERCICIO_INEXISTENTE_MODELO);
            }
            if (treino.existeExercicioRepetido()) {
                var nomeExercicio = exercicios.buscarPorIdentificador(treino.buscarExercicioRepetido()).getNome();
                throw new IllegalArgumentException(String.format(EXERCICIO_REPETIDO_MODELO, nomeExercicio, treino.nome()));
            }
        }
    }

    private void bloquearAlunoDoPlano(PlanoDeTreinoIdentificador identificador) {
        var aluno = repositorio.buscarPorIdentificador(identificador).getAluno();
        alunos.bloquearPorUsuario(alunos.buscarPorIdentificador(aluno).getUsuario());
    }

    private PersonalIdentificador personalAutenticado() {
        return personais.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow();
    }

    private AlunoIdentificador alunoAutenticado() {
        return alunos.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow();
    }
}
