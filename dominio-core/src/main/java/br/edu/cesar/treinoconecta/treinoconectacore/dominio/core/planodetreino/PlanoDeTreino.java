package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;

public final class PlanoDeTreino implements IAgregado<PlanoDeTreinoIdentificador> {
    public static final int TAMANHO_NOME = 100;
    public static final int FREQUENCIA_MAXIMA = 7;

    private final Optional<PlanoDeTreinoIdentificador> identificador;
    private final AlunoIdentificador aluno;
    private final PersonalIdentificador personal;
    private final String nome;
    private final int frequenciaSemanal;
    private final List<Treino> treinos;
    private final SituacaoPlano situacao;
    private final int versao;
    private final Optional<PlanoDeTreinoIdentificador> planoAnterior;
    private final Instant criacao;
    private final Optional<Instant> encerramento;

    public PlanoDeTreino(Optional<PlanoDeTreinoIdentificador> identificador, AlunoIdentificador aluno,
            PersonalIdentificador personal, String nome, int frequenciaSemanal, List<Treino> treinos,
            SituacaoPlano situacao, int versao, Optional<PlanoDeTreinoIdentificador> planoAnterior, Instant criacao,
            Optional<Instant> encerramento) {
        this.identificador = Objects.requireNonNull(identificador);
        this.aluno = Objects.requireNonNull(aluno);
        this.personal = Objects.requireNonNull(personal);
        this.nome = Validate.notBlank(nome, PlanoDeTreinoServico.NOME_INVALIDO);
        Validate.isTrue(nome.length() <= TAMANHO_NOME, PlanoDeTreinoServico.NOME_INVALIDO);
        Validate.isTrue(frequenciaSemanal >= 1 && frequenciaSemanal <= FREQUENCIA_MAXIMA,
                PlanoDeTreinoServico.FREQUENCIA_INVALIDA);
        this.frequenciaSemanal = frequenciaSemanal;
        this.treinos = List.copyOf(treinos);
        validarDias(this.treinos);
        this.situacao = Objects.requireNonNull(situacao);
        Validate.isTrue(versao >= 1, PlanoDeTreinoServico.VERSAO_INVALIDA);
        this.versao = versao;
        this.planoAnterior = Objects.requireNonNull(planoAnterior);
        this.criacao = Objects.requireNonNull(criacao);
        this.encerramento = Objects.requireNonNull(encerramento);
    }

    private static void validarDias(List<Treino> treinos) {
        var dias = new HashSet<DiaSemana>();
        for (var treino : treinos) {
            Validate.isTrue(dias.add(treino.dia()), PlanoDeTreinoServico.DIA_REPETIDO_MODELO, treino.dia());
        }
    }

    public static PlanoDeTreino criar(AlunoIdentificador aluno, PersonalIdentificador personal, String nome,
            int frequenciaSemanal, List<Treino> treinos, Instant agora) {
        return new PlanoDeTreino(Optional.empty(), aluno, personal, nome, frequenciaSemanal, treinos,
                SituacaoPlano.RASCUNHO, 1, Optional.empty(), agora, Optional.empty());
    }

    public PlanoDeTreino ativar(PersonalIdentificador responsavel) {
        validarResponsavel(responsavel);
        Validate.validState(situacao == SituacaoPlano.RASCUNHO, PlanoDeTreinoServico.SOMENTE_RASCUNHO_ATIVA);
        validarConteudo();
        return comSituacao(SituacaoPlano.ATIVO, Optional.empty());
    }

    public PlanoDeTreino encerrar(PersonalIdentificador responsavel, Instant agora) {
        Objects.requireNonNull(agora);
        validarResponsavel(responsavel);
        Validate.validState(editavel(), PlanoDeTreinoServico.PLANO_JA_FINALIZADO);
        return comSituacao(SituacaoPlano.ENCERRADO, Optional.of(agora));
    }

    public PlanoDeTreino substituir(PersonalIdentificador responsavel, Instant agora) {
        Objects.requireNonNull(agora);
        validarResponsavel(responsavel);
        Validate.validState(editavel(), PlanoDeTreinoServico.PLANO_JA_FINALIZADO);
        return comSituacao(SituacaoPlano.SUBSTITUIDO, Optional.of(agora));
    }

    /** Monta a nova versão. O plano atual permanece igual até ser substituído. */
    public PlanoDeTreino revisar(PersonalIdentificador responsavel, String novoNome, int novaFrequencia,
            List<Treino> novosTreinos, Instant agora) {
        var revisado = new PlanoDeTreino(Optional.empty(), aluno, personal, novoNome, novaFrequencia, novosTreinos,
                situacao, versao + 1, identificador, agora, Optional.empty());
        validarResponsavel(responsavel);
        Validate.validState(editavel(), PlanoDeTreinoServico.PLANO_NAO_ALTERAVEL);
        if (situacao == SituacaoPlano.ATIVO) {
            revisado.validarConteudo();
        }
        return revisado;
    }

    public void validarExecucao(AlunoIdentificador executor, DiaSemana dia, ExercicioIdentificador exercicio,
            int series, int repeticoes) {
        Objects.requireNonNull(executor);
        if (!aluno.equals(executor)) {
            throw new NaoAutorizadoExcecao(PlanoDeTreinoServico.PLANO_DE_OUTRO_ALUNO);
        }
        var serie = buscarSeriePrescrita(dia, exercicio);
        Validate.isTrue(series <= serie.quantidade(), PlanoDeTreinoServico.SERIES_ACIMA_DO_PLANO_MODELO,
                serie.quantidade());
        Validate.isTrue(repeticoes <= serie.repeticoesMaximas(), PlanoDeTreinoServico.REPETICOES_ACIMA_DO_PLANO_MODELO,
                serie.repeticoesMaximas());
        Validate.validState(situacao != SituacaoPlano.ENCERRADO, PlanoDeTreinoServico.PLANO_ENCERRADO);
        Validate.validState(situacao == SituacaoPlano.ATIVO, PlanoDeTreinoServico.PLANO_NAO_ATIVO);
    }

    /** Maior limite de repetições prescrito para o exercício neste plano. */
    public Optional<Integer> limiteRepeticoes(ExercicioIdentificador exercicio) {
        Objects.requireNonNull(exercicio);
        Optional<Integer> limite = Optional.empty();
        for (var treino : treinos) {
            var serie = treino.buscarSerie(exercicio);
            if (serie.isPresent() && (limite.isEmpty() || serie.get().repeticoesMaximas() > limite.get())) {
                limite = Optional.of(serie.get().repeticoesMaximas());
            }
        }
        return limite;
    }

    private Serie buscarSeriePrescrita(DiaSemana dia, ExercicioIdentificador exercicio) {
        Objects.requireNonNull(dia);
        Objects.requireNonNull(exercicio);
        for (var treino : treinos) {
            if (treino.dia() == dia && treino.buscarSerie(exercicio).isPresent()) {
                return treino.buscarSerie(exercicio).get();
            }
        }
        throw new IllegalArgumentException(
                String.format(PlanoDeTreinoServico.EXERCICIO_FORA_DO_TREINO_MODELO, exercicio, dia));
    }

    private void validarResponsavel(PersonalIdentificador responsavel) {
        Objects.requireNonNull(responsavel);
        if (!personal.equals(responsavel)) {
            throw new NaoAutorizadoExcecao(PlanoDeTreinoServico.PLANO_DE_OUTRO_PERSONAL);
        }
    }

    private void validarConteudo() {
        Validate.validState(!treinos.isEmpty(), PlanoDeTreinoServico.PLANO_VAZIO);
        for (var treino : treinos) {
            Validate.validState(treino.possuiExercicios(), PlanoDeTreinoServico.TREINO_SEM_EXERCICIOS_MODELO,
                    treino.nome());
        }
        Validate.validState(frequenciaSemanal == treinos.size(), PlanoDeTreinoServico.FREQUENCIA_DIVERGENTE_MODELO,
                frequenciaSemanal, treinos.size());
    }

    private boolean editavel() {
        return situacao == SituacaoPlano.RASCUNHO || situacao == SituacaoPlano.ATIVO;
    }

    private PlanoDeTreino comSituacao(SituacaoPlano novaSituacao, Optional<Instant> novoEncerramento) {
        return new PlanoDeTreino(identificador, aluno, personal, nome, frequenciaSemanal, treinos, novaSituacao, versao,
                planoAnterior, criacao, novoEncerramento);
    }

    public Optional<PlanoDeTreinoIdentificador> getIdentificador() { return identificador; }
    public AlunoIdentificador getAluno() { return aluno; }
    public PersonalIdentificador getPersonal() { return personal; }
    public String getNome() { return nome; }
    public int getFrequenciaSemanal() { return frequenciaSemanal; }
    public List<Treino> getTreinos() { return treinos; }
    public SituacaoPlano getSituacao() { return situacao; }
    public int getVersao() { return versao; }
    public Optional<PlanoDeTreinoIdentificador> getPlanoAnterior() { return planoAnterior; }
    public Instant getCriacao() { return criacao; }
    public Optional<Instant> getEncerramento() { return encerramento; }
}
