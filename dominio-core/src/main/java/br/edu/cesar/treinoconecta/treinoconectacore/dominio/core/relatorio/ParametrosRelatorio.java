package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;

/** Valores definidos pela academia para os relatórios de desempenho. */
public final class ParametrosRelatorio {
    /** O período de análise pode ter até 12 meses. */
    public static final int PERIODO_MAXIMO_DIAS = 366;

    /** Períodos maiores que este limite não podem ser agrupados por dia. */
    public static final int AGRUPAMENTO_DIARIO_MAXIMO_DIAS = 90;

    /** Prazo, contado do fim da matrícula, para que a nova matrícula conte como renovação. */
    public static final int TOLERANCIA_RENOVACAO_DIAS = 10;

    /** Dias depois do vencimento até a mensalidade em aberto contar como inadimplência. */
    public static final int CARENCIA_INADIMPLENCIA_DIAS = 5;

    public static final int DIAS_SEM_ENTRADA_RISCO = 14;
    public static final BigDecimal QUEDA_FREQUENCIA_RISCO_PERCENTUAL = new BigDecimal("50");

    public static final BigDecimal OCUPACAO_PICO_PERCENTUAL = new BigDecimal("85");
    public static final BigDecimal OCUPACAO_BAIXA_PERCENTUAL = new BigDecimal("30");

    /** Capacidade da sala de musculação em cada faixa de horário. */
    public static final int CAPACIDADE_SALA = 7;
    public static final int VAGAS_ESTACIONAMENTO = 4;

    public static final int DURACAO_FAIXA_HORAS = 2;
    public static final LocalTime ABERTURA = LocalTime.of(6, 0);
    public static final LocalTime FECHAMENTO = LocalTime.of(22, 0);
    public static final LocalTime ABERTURA_FIM_DE_SEMANA = LocalTime.of(8, 0);
    public static final LocalTime FECHAMENTO_SABADO = LocalTime.of(14, 0);
    public static final LocalTime FECHAMENTO_DOMINGO = LocalTime.of(12, 0);

    private ParametrosRelatorio() {
    }

    /** Indica se a academia funciona durante toda a faixa que começa no horário informado. */
    public static boolean abertaNaFaixa(DayOfWeek dia, LocalTime inicioFaixa) {
        var fimFaixa = inicioFaixa.plusHours(DURACAO_FAIXA_HORAS);
        return switch (dia) {
        case SATURDAY -> !inicioFaixa.isBefore(ABERTURA_FIM_DE_SEMANA) && !fimFaixa.isAfter(FECHAMENTO_SABADO);
        case SUNDAY -> !inicioFaixa.isBefore(ABERTURA_FIM_DE_SEMANA) && !fimFaixa.isAfter(FECHAMENTO_DOMINGO);
        default -> !inicioFaixa.isBefore(ABERTURA) && !fimFaixa.isAfter(FECHAMENTO);
        };
    }
}
