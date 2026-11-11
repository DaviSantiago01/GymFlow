package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core;

import java.math.BigDecimal;
import java.time.ZoneId;

/** Valores definidos pela academia para treinos e avaliações físicas. */
public final class ParametrosAcademia {
    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    public static final int INTERVALO_MINIMO_SEGUNDOS = 15;
    public static final int INTERVALO_MAXIMO_SEGUNDOS = 300;

    public static final int SERIES_MINIMAS_FORCA = 3;
    public static final int SERIES_MAXIMAS_FORCA = 6;
    public static final int REPETICOES_MINIMAS_FORCA = 1;
    public static final int REPETICOES_MAXIMAS_FORCA = 6;

    public static final int SERIES_MINIMAS_HIPERTROFIA = 3;
    public static final int SERIES_MAXIMAS_HIPERTROFIA = 5;
    public static final int REPETICOES_MINIMAS_HIPERTROFIA = 6;
    public static final int REPETICOES_MAXIMAS_HIPERTROFIA = 12;

    public static final int SERIES_MINIMAS_RESISTENCIA = 2;
    public static final int SERIES_MAXIMAS_RESISTENCIA = 4;
    public static final int REPETICOES_MINIMAS_RESISTENCIA = 12;
    public static final int REPETICOES_MAXIMAS_RESISTENCIA = 20;

    public static final int EXECUCOES_PARA_PROGRESSAO = 2;
    public static final BigDecimal PERCENTUAL_PROGRESSAO = new BigDecimal("5");

    public static final BigDecimal PESO_MINIMO = new BigDecimal("20");
    public static final BigDecimal PESO_MAXIMO = new BigDecimal("400");
    public static final BigDecimal ALTURA_MINIMA = new BigDecimal("100");
    public static final BigDecimal ALTURA_MAXIMA = new BigDecimal("250");
    public static final BigDecimal MEDIDA_MINIMA = new BigDecimal("1");
    public static final BigDecimal MEDIDA_MAXIMA = new BigDecimal("300");

    public static final BigDecimal IMC_LIMITE_ABAIXO_DO_PESO = new BigDecimal("18.5");
    public static final BigDecimal IMC_LIMITE_NORMAL = new BigDecimal("25");
    public static final BigDecimal IMC_LIMITE_SOBREPESO = new BigDecimal("30");
    public static final BigDecimal IMC_LIMITE_OBESIDADE_GRAU_I = new BigDecimal("35");
    public static final BigDecimal IMC_LIMITE_OBESIDADE_GRAU_II = new BigDecimal("40");

    public static final BigDecimal MARGEM_ESTABILIDADE_PERCENTUAL = new BigDecimal("1");
    public static final int AVALIACOES_ANTERIORES_PARA_TENDENCIA = 2;
    public static final BigDecimal PERCENTUAL_META_PROXIMA = new BigDecimal("75");

    private ParametrosAcademia() {
    }
}
