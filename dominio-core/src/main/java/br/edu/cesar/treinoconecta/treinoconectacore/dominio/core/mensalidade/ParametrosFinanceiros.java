package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade;

import java.math.BigDecimal;

/** Valores definidos pela academia para cobrança de mensalidades. */
public final class ParametrosFinanceiros {
    /** O vencimento é contado a partir do início do período de cobrança. */
    public static final int PRAZO_PAGAMENTO_DIAS = 5;

    /** Multa aplicada uma única vez quando a mensalidade entra em atraso. */
    public static final BigDecimal PERCENTUAL_MULTA = new BigDecimal("2");

    /** Juros simples ao mês, proporcionais aos dias de atraso. */
    public static final BigDecimal PERCENTUAL_JUROS_MENSAL = new BigDecimal("1");
    public static final int DIAS_DO_MES_PARA_JUROS = 30;

    public static final int TAMANHO_MOTIVO_ESTORNO = 250;

    private ParametrosFinanceiros() {
    }
}
