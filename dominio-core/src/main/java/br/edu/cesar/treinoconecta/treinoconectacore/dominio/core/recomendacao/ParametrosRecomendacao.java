package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao;

import java.math.BigDecimal;

/** Valores definidos pela academia para as recomendações personalizadas. */
public final class ParametrosRecomendacao {
    public static final int INTERVALO_REAVALIACAO_DIAS = 90;
    public static final int EXECUCOES_ESTAGNACAO = 4;
    public static final BigDecimal PERCENTUAL_FREQUENCIA_MINIMA = new BigDecimal("50");
    public static final int SEMANAS_FREQUENCIA_BAIXA = 3;
    public static final int SEMANAS_EXIBIDAS_NA_JUSTIFICATIVA = 6;
    public static final int AVALIACOES_TENDENCIA = 2;
    public static final int CARENCIA_RECUSA_DIAS = 30;
    public static final int VALIDADE_DIAS = 14;
    public static final int MAXIMO_EXIBIDAS = 5;
    public static final BigDecimal TAXA_ACEITACAO_MINIMA = new BigDecimal("30");
    public static final int RESPOSTAS_CONSIDERADAS = 10;
    public static final int MESES_PARA_OFERTA = 3;
    public static final int MAXIMO_AULAS_SUGERIDAS = 3;
    public static final int PENALIDADE_PRIORIDADE = 10;

    private ParametrosRecomendacao() {
    }
}
