package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao;

/**
 * AGUARDANDO_APROVACAO: sugestão de treino enviada ao personal e ainda não exibida ao aluno. PENDENTE: exibida ao aluno,
 * aguardando aceite ou recusa. APROVADA e REJEITADA são decisões do personal; ACEITA e RECUSADA, do aluno. EXPIRADA:
 * ninguém respondeu dentro da validade.
 */
public enum SituacaoRecomendacao {
    AGUARDANDO_APROVACAO, PENDENTE, APROVADA, ACEITA, RECUSADA, REJEITADA, EXPIRADA;

    public boolean emAberto() {
        return this == AGUARDANDO_APROVACAO || this == PENDENTE;
    }

    public boolean recusada() {
        return this == RECUSADA || this == REJEITADA;
    }

    public boolean aceita() {
        return this == ACEITA || this == APROVADA;
    }
}
