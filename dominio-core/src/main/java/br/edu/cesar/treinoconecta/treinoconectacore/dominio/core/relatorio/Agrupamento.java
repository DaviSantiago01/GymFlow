package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

/** RN004: os resultados podem ser agrupados por dia, semana ou mês. */
public enum Agrupamento {
    DIA(1), SEMANA(7), MES(28);

    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter MES_ANO = DateTimeFormatter.ofPattern("MMM/yyyy", Locale.of("pt", "BR"));

    private final int diasMinimos;

    Agrupamento(int diasMinimos) {
        this.diasMinimos = diasMinimos;
    }

    public void validarPara(PeriodoAnalise periodo) {
        Objects.requireNonNull(periodo);
        Validate.isTrue(periodo.dias() >= diasMinimos, RelatorioServico.AGRUPAMENTO_MAIOR_QUE_PERIODO);
        Validate.isTrue(this != DIA || periodo.dias() <= ParametrosRelatorio.AGRUPAMENTO_DIARIO_MAXIMO_DIAS,
                RelatorioServico.AGRUPAMENTO_DIARIO_LONGO_MODELO, ParametrosRelatorio.AGRUPAMENTO_DIARIO_MAXIMO_DIAS);
    }

    /** Divide o período em grupos consecutivos. O último grupo pode ser menor que os demais. */
    public List<GrupoPeriodo> dividir(PeriodoAnalise periodo) {
        Objects.requireNonNull(periodo);
        var grupos = new ArrayList<GrupoPeriodo>();
        var inicio = periodo.inicio();
        while (!inicio.isAfter(periodo.fim())) {
            var fim = switch (this) {
            case DIA -> inicio;
            case SEMANA -> inicio.plusDays(6);
            case MES -> inicio.withDayOfMonth(inicio.lengthOfMonth());
            };
            if (fim.isAfter(periodo.fim())) {
                fim = periodo.fim();
            }
            grupos.add(new GrupoPeriodo(rotulo(inicio, fim), new PeriodoAnalise(inicio, fim)));
            inicio = fim.plusDays(1);
        }
        return grupos;
    }

    private String rotulo(LocalDate inicio, LocalDate fim) {
        return switch (this) {
        case DIA -> inicio.format(DIA_MES);
        case SEMANA -> inicio.equals(fim) ? inicio.format(DIA_MES) : inicio.format(DIA_MES) + " a " + fim.format(DIA_MES);
        case MES -> inicio.format(MES_ANO).replace(".", "");
        };
    }

    public record GrupoPeriodo(String rotulo, PeriodoAnalise periodo) {
    }
}
