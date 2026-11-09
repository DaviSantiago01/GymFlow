package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

public record ComparacaoIndicador(IndicadorFisico indicador, BigDecimal valorReferencia, BigDecimal valorAtual,
        BigDecimal diferencaAbsoluta, BigDecimal diferencaPercentual, BigDecimal evolucaoPercentual,
        InterpretacaoEvolucao interpretacao) implements IObjetoValor {
    private static final BigDecimal CEM = new BigDecimal("100");

    public static ComparacaoIndicador comparar(IndicadorFisico indicador, BigDecimal referencia, BigDecimal atual) {
        Objects.requireNonNull(indicador);
        var evolucao = variacaoPercentual(referencia, atual);
        return new ComparacaoIndicador(indicador, referencia, atual, atual.subtract(referencia).abs(), evolucao.abs(),
                evolucao, indicador.interpretar(evolucao));
    }

    public static BigDecimal variacaoPercentual(BigDecimal referencia, BigDecimal atual) {
        Objects.requireNonNull(referencia);
        Objects.requireNonNull(atual);
        return atual.subtract(referencia).multiply(CEM).divide(referencia, 2, RoundingMode.HALF_UP);
    }
}
