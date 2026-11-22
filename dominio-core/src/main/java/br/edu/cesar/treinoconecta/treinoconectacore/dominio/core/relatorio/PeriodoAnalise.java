package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

/** Intervalo fechado [início, fim] analisado por um relatório. */
public record PeriodoAnalise(LocalDate inicio, LocalDate fim) implements IObjetoValor {
    public PeriodoAnalise {
        Validate.notNull(inicio, RelatorioServico.PERIODO_OBRIGATORIO);
        Validate.notNull(fim, RelatorioServico.PERIODO_OBRIGATORIO);
        Validate.isTrue(!inicio.isAfter(fim), RelatorioServico.PERIODO_INVERTIDO);
    }

    /** RN002: além de válido, o período não pode terminar depois de hoje nem ultrapassar o limite da academia. */
    public static PeriodoAnalise validar(LocalDate inicio, LocalDate fim, LocalDate hoje) {
        var periodo = new PeriodoAnalise(inicio, fim);
        Objects.requireNonNull(hoje);
        Validate.isTrue(!fim.isAfter(hoje), RelatorioServico.PERIODO_FUTURO);
        Validate.isTrue(periodo.dias() <= ParametrosRelatorio.PERIODO_MAXIMO_DIAS, RelatorioServico.PERIODO_LONGO);
        return periodo;
    }

    public long dias() {
        return ChronoUnit.DAYS.between(inicio, fim) + 1;
    }

    public BigDecimal semanas() {
        return BigDecimal.valueOf(dias()).divide(BigDecimal.valueOf(7), 6, RoundingMode.HALF_UP);
    }

    /** RN011: período imediatamente anterior e de mesma duração. */
    public PeriodoAnalise anterior() {
        return new PeriodoAnalise(inicio.minusDays(dias()), inicio.minusDays(1));
    }

    public boolean contem(LocalDate data) {
        Objects.requireNonNull(data);
        return !data.isBefore(inicio) && !data.isAfter(fim);
    }

    /** Parte do período que se sobrepõe ao intervalo [início, fim] informado, se houver. */
    public Optional<PeriodoAnalise> intersecao(LocalDate outroInicio, LocalDate outroFim) {
        var maiorInicio = outroInicio.isAfter(inicio) ? outroInicio : inicio;
        var menorFim = outroFim.isBefore(fim) ? outroFim : fim;
        return maiorInicio.isAfter(menorFim) ? Optional.empty()
                : Optional.of(new PeriodoAnalise(maiorInicio, menorFim));
    }
}
