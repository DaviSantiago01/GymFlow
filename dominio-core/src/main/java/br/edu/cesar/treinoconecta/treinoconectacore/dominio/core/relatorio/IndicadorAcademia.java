package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

/** Indicadores da academia. Taxas são percentuais e a variação delas é apresentada em pontos percentuais. */
public enum IndicadorAcademia {
    ALUNOS_ATIVOS("Alunos ativos no fim do período", SentidoIndicador.MAIOR_MELHOR, Unidade.QUANTIDADE),
    NOVAS_MATRICULAS("Novas matrículas", SentidoIndicador.MAIOR_MELHOR, Unidade.QUANTIDADE),
    REATIVACOES("Reativações", SentidoIndicador.MAIOR_MELHOR, Unidade.QUANTIDADE),
    FREQUENCIA_MEDIA("Frequência média por semana", SentidoIndicador.MAIOR_MELHOR, Unidade.DECIMAL),
    TAXA_EVASAO("Taxa de evasão", SentidoIndicador.MENOR_MELHOR, Unidade.PERCENTUAL),
    TAXA_RENOVACAO("Taxa de renovação", SentidoIndicador.MAIOR_MELHOR, Unidade.PERCENTUAL),
    RECEITA_LIQUIDA("Receita líquida", SentidoIndicador.MAIOR_MELHOR, Unidade.MOEDA),
    TAXA_INADIMPLENCIA("Taxa de inadimplência", SentidoIndicador.MENOR_MELHOR, Unidade.PERCENTUAL),
    ALUNOS_EM_RISCO("Alunos em risco de evasão", SentidoIndicador.MENOR_MELHOR, Unidade.QUANTIDADE);

    private final String descricao;
    private final SentidoIndicador sentido;
    private final Unidade unidade;

    IndicadorAcademia(String descricao, SentidoIndicador sentido, Unidade unidade) {
        this.descricao = descricao;
        this.sentido = sentido;
        this.unidade = unidade;
    }

    public String getDescricao() { return descricao; }
    public SentidoIndicador getSentido() { return sentido; }
    public Unidade getUnidade() { return unidade; }

    public enum Unidade {
        QUANTIDADE, DECIMAL, PERCENTUAL, MOEDA
    }
}
