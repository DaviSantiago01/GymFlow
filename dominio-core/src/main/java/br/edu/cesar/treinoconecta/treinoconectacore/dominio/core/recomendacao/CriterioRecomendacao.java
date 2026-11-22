package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao;

/** Critério que originou a recomendação. Os que alteram o plano de treino dependem da aprovação do personal (RN011). */
public enum CriterioRecomendacao {
    REAVALIACAO_FISICA(CategoriaRecomendacao.ACOMPANHAMENTO_FISICO, false),
    REENGAJAMENTO(CategoriaRecomendacao.FREQUENCIA_HORARIOS, false),
    SUBSTITUICAO_EXERCICIO(CategoriaRecomendacao.AJUSTES_TREINO, true),
    REVISAO_PLANO(CategoriaRecomendacao.AJUSTES_TREINO, true),
    AULA_COMPATIVEL(CategoriaRecomendacao.AULAS_COLETIVAS, false),
    OFERTA_PLANO(CategoriaRecomendacao.PLANOS_SERVICOS, false);

    private final CategoriaRecomendacao categoria;
    private final boolean exigeAprovacao;

    CriterioRecomendacao(CategoriaRecomendacao categoria, boolean exigeAprovacao) {
        this.categoria = categoria;
        this.exigeAprovacao = exigeAprovacao;
    }

    public CategoriaRecomendacao getCategoria() { return categoria; }
    public boolean isExigeAprovacao() { return exigeAprovacao; }
}
