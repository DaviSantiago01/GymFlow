package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;

/**
 * Recomendação gerada para um aluno a partir de um critério. Toda recomendação tem justificativa (RN012) e validade
 * (RN015), e nunca é excluída: a situação final, quem respondeu e quando ficam no histórico (RN019).
 */
public final class Recomendacao implements IAgregado<RecomendacaoIdentificador> {
    public static final int TAMANHO_ITEM = 60;
    public static final int TAMANHO_TITULO = 120;
    public static final int TAMANHO_TEXTO = 500;

    private final Optional<RecomendacaoIdentificador> identificador;
    private final AlunoIdentificador aluno;
    private final CriterioRecomendacao criterio;
    private final String item;
    private final String titulo;
    private final String descricao;
    private final String justificativa;
    private final List<Evidencia> evidencias;
    private final ReferenciasRecomendacao referencias;
    private final Optional<SugestaoSubstituicao> sugestao;
    private final Instant geracao;
    private final LocalDate validade;
    private final SituacaoRecomendacao situacao;
    private final Optional<RespostaRecomendacao> resposta;
    private final Optional<PlanoDeTreinoIdentificador> planoGerado;
    private final Optional<Instant> ciencia;

    public Recomendacao(Optional<RecomendacaoIdentificador> identificador, AlunoIdentificador aluno,
            CriterioRecomendacao criterio, String item, String titulo, String descricao, String justificativa,
            List<Evidencia> evidencias, ReferenciasRecomendacao referencias, Optional<SugestaoSubstituicao> sugestao,
            Instant geracao, LocalDate validade, SituacaoRecomendacao situacao, Optional<RespostaRecomendacao> resposta,
            Optional<PlanoDeTreinoIdentificador> planoGerado, Optional<Instant> ciencia) {
        this.identificador = Objects.requireNonNull(identificador);
        this.aluno = Objects.requireNonNull(aluno);
        this.criterio = Objects.requireNonNull(criterio);
        this.item = texto(item, TAMANHO_ITEM, "Item da recomendação inválido");
        this.titulo = texto(titulo, TAMANHO_TITULO, "Título da recomendação inválido");
        this.descricao = texto(descricao, TAMANHO_TEXTO, "Descrição da recomendação inválida");
        this.justificativa = texto(justificativa, TAMANHO_TEXTO, RecomendacaoServico.SEM_JUSTIFICATIVA);
        this.evidencias = List.copyOf(evidencias);
        this.referencias = Objects.requireNonNull(referencias);
        this.sugestao = Objects.requireNonNull(sugestao);
        this.geracao = Objects.requireNonNull(geracao);
        this.validade = Objects.requireNonNull(validade);
        this.situacao = Objects.requireNonNull(situacao);
        this.resposta = Objects.requireNonNull(resposta);
        this.planoGerado = Objects.requireNonNull(planoGerado);
        this.ciencia = Objects.requireNonNull(ciencia);
        if (criterio.isExigeAprovacao()) {
            Validate.isTrue(referencias.personal().isPresent() && referencias.planoTreino().isPresent(),
                    RecomendacaoServico.SEM_PERSONAL_RESPONSAVEL);
            Validate.isTrue(situacao != SituacaoRecomendacao.PENDENTE && situacao != SituacaoRecomendacao.ACEITA
                    && situacao != SituacaoRecomendacao.RECUSADA, "Situação incompatível com sugestão de treino");
        } else {
            Validate.isTrue(situacao != SituacaoRecomendacao.AGUARDANDO_APROVACAO && situacao != SituacaoRecomendacao.APROVADA
                    && situacao != SituacaoRecomendacao.REJEITADA, "Situação incompatível com recomendação ao aluno");
        }
        Validate.isTrue(criterio != CriterioRecomendacao.SUBSTITUICAO_EXERCICIO || sugestao.isPresent(),
                "A substituição de exercício precisa da sugestão");
        boolean respondida = situacao != SituacaoRecomendacao.PENDENTE && situacao != SituacaoRecomendacao.AGUARDANDO_APROVACAO
                && situacao != SituacaoRecomendacao.EXPIRADA;
        Validate.isTrue(respondida == resposta.isPresent(), "Resposta da recomendação inconsistente");
        Validate.isTrue(!validade.isBefore(geracao.atZone(ParametrosAcademia.FUSO).toLocalDate()),
                "A validade não pode anteceder a geração");
    }

    private static String texto(String valor, int tamanho, String mensagem) {
        Validate.notBlank(valor, mensagem);
        var normalizado = valor.strip();
        Validate.isTrue(normalizado.length() <= tamanho, mensagem);
        return normalizado;
    }

    /** RN011: sugestões que alteram o treino vão para o personal; as demais são exibidas ao aluno. */
    public static Recomendacao gerar(AlunoIdentificador aluno, CriterioRecomendacao criterio, String item, String titulo,
            String descricao, String justificativa, List<Evidencia> evidencias, ReferenciasRecomendacao referencias,
            Optional<SugestaoSubstituicao> sugestao, Instant agora, LocalDate hoje) {
        var situacao = criterio.isExigeAprovacao() ? SituacaoRecomendacao.AGUARDANDO_APROVACAO : SituacaoRecomendacao.PENDENTE;
        return new Recomendacao(Optional.empty(), aluno, criterio, item, titulo, descricao, justificativa, evidencias,
                referencias, sugestao, agora, hoje.plusDays(ParametrosRecomendacao.VALIDADE_DIAS), situacao,
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    public CategoriaRecomendacao getCategoria() {
        return criterio.getCategoria();
    }

    public boolean vencidaEm(LocalDate hoje) {
        return situacao.emAberto() && validade.isBefore(hoje);
    }

    /** RN015: sem resposta dentro do prazo, deixa de ser exibida, mas permanece no histórico. */
    public Recomendacao expirar(LocalDate hoje) {
        Validate.validState(vencidaEm(hoje), "Somente recomendações em aberto e vencidas podem expirar");
        return comSituacao(SituacaoRecomendacao.EXPIRADA, Optional.empty(), planoGerado);
    }

    public Recomendacao aceitar(AlunoIdentificador respondente, String nome, Instant agora, LocalDate hoje) {
        validarRespostaDoAluno(respondente, hoje);
        return comSituacao(SituacaoRecomendacao.ACEITA,
                Optional.of(new RespostaRecomendacao(agora, nome, Optional.empty(), Optional.empty())), planoGerado);
    }

    public Recomendacao recusar(AlunoIdentificador respondente, String nome, Optional<String> motivo, Instant agora,
            LocalDate hoje) {
        validarRespostaDoAluno(respondente, hoje);
        return comSituacao(SituacaoRecomendacao.RECUSADA,
                Optional.of(new RespostaRecomendacao(agora, nome, motivo, Optional.empty())), planoGerado);
    }

    private void validarRespostaDoAluno(AlunoIdentificador respondente, LocalDate hoje) {
        Objects.requireNonNull(respondente);
        if (!aluno.equals(respondente)) {
            throw new NaoAutorizadoExcecao(RecomendacaoServico.RECOMENDACAO_DE_OUTRO_ALUNO);
        }
        Validate.validState(situacao == SituacaoRecomendacao.PENDENTE, RecomendacaoServico.NAO_PENDENTE);
        Validate.validState(!validade.isBefore(hoje), RecomendacaoServico.RECOMENDACAO_EXPIRADA);
    }

    /** RN011 e RN020: aprovada pelo personal responsável; a nova versão do plano, quando houver, fica vinculada. */
    public Recomendacao aprovar(PersonalIdentificador personal, String nome, Optional<String> ajuste,
            Optional<PlanoDeTreinoIdentificador> novaVersao, Instant agora, LocalDate hoje) {
        validarDecisaoDoPersonal(personal, hoje);
        return comSituacao(SituacaoRecomendacao.APROVADA,
                Optional.of(new RespostaRecomendacao(agora, nome, Optional.empty(), ajuste)), novaVersao);
    }

    public Recomendacao rejeitar(PersonalIdentificador personal, String nome, Optional<String> motivo, Instant agora,
            LocalDate hoje) {
        validarDecisaoDoPersonal(personal, hoje);
        return comSituacao(SituacaoRecomendacao.REJEITADA,
                Optional.of(new RespostaRecomendacao(agora, nome, motivo, Optional.empty())), planoGerado);
    }

    private void validarDecisaoDoPersonal(PersonalIdentificador personal, LocalDate hoje) {
        Objects.requireNonNull(personal);
        if (!referencias.personal().filter(personal::equals).isPresent()) {
            throw new NaoAutorizadoExcecao(RecomendacaoServico.SUGESTAO_DE_OUTRO_PERSONAL);
        }
        Validate.validState(situacao == SituacaoRecomendacao.AGUARDANDO_APROVACAO, RecomendacaoServico.NAO_AGUARDA_APROVACAO);
        Validate.validState(!validade.isBefore(hoje), RecomendacaoServico.RECOMENDACAO_EXPIRADA);
    }

    /** O aluno confirma que viu a mudança aprovada pelo personal; a recomendação sai da lista dele. */
    public Recomendacao registrarCiencia(AlunoIdentificador respondente, Instant agora) {
        Objects.requireNonNull(respondente);
        if (!aluno.equals(respondente)) {
            throw new NaoAutorizadoExcecao(RecomendacaoServico.RECOMENDACAO_DE_OUTRO_ALUNO);
        }
        Validate.validState(situacao == SituacaoRecomendacao.APROVADA && ciencia.isEmpty(), RecomendacaoServico.NAO_APROVADA);
        return new Recomendacao(identificador, aluno, criterio, item, titulo, descricao, justificativa, evidencias,
                referencias, sugestao, geracao, validade, situacao, resposta, planoGerado, Optional.of(agora));
    }

    /** RN011 e RN016: o aluno vê as pendentes e as mudanças aprovadas de que ainda não tomou ciência. */
    public boolean visivelAoAluno(LocalDate hoje) {
        if (situacao == SituacaoRecomendacao.PENDENTE) {
            return !validade.isBefore(hoje);
        }
        return situacao == SituacaoRecomendacao.APROVADA && ciencia.isEmpty();
    }

    private Recomendacao comSituacao(SituacaoRecomendacao nova, Optional<RespostaRecomendacao> novaResposta,
            Optional<PlanoDeTreinoIdentificador> novoPlano) {
        return new Recomendacao(identificador, aluno, criterio, item, titulo, descricao, justificativa, evidencias,
                referencias, sugestao, geracao, validade, nova, novaResposta, novoPlano, ciencia);
    }

    public Optional<RecomendacaoIdentificador> getIdentificador() { return identificador; }
    public AlunoIdentificador getAluno() { return aluno; }
    public CriterioRecomendacao getCriterio() { return criterio; }
    public String getItem() { return item; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getJustificativa() { return justificativa; }
    public List<Evidencia> getEvidencias() { return evidencias; }
    public ReferenciasRecomendacao getReferencias() { return referencias; }
    public Optional<SugestaoSubstituicao> getSugestao() { return sugestao; }
    public Instant getGeracao() { return geracao; }
    public LocalDate getValidade() { return validade; }
    public SituacaoRecomendacao getSituacao() { return situacao; }
    public Optional<RespostaRecomendacao> getResposta() { return resposta; }
    public Optional<PlanoDeTreinoIdentificador> getPlanoGerado() { return planoGerado; }
    public Optional<Instant> getCiencia() { return ciencia; }
}
