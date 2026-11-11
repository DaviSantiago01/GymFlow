package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino;

import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;

public enum ObjetivoTreino {
    FORCA(ParametrosAcademia.SERIES_MINIMAS_FORCA, ParametrosAcademia.SERIES_MAXIMAS_FORCA,
            ParametrosAcademia.REPETICOES_MINIMAS_FORCA, ParametrosAcademia.REPETICOES_MAXIMAS_FORCA),
    HIPERTROFIA(ParametrosAcademia.SERIES_MINIMAS_HIPERTROFIA, ParametrosAcademia.SERIES_MAXIMAS_HIPERTROFIA,
            ParametrosAcademia.REPETICOES_MINIMAS_HIPERTROFIA, ParametrosAcademia.REPETICOES_MAXIMAS_HIPERTROFIA),
    RESISTENCIA(ParametrosAcademia.SERIES_MINIMAS_RESISTENCIA, ParametrosAcademia.SERIES_MAXIMAS_RESISTENCIA,
            ParametrosAcademia.REPETICOES_MINIMAS_RESISTENCIA, ParametrosAcademia.REPETICOES_MAXIMAS_RESISTENCIA);

    private final int seriesMinimas;
    private final int seriesMaximas;
    private final int repeticoesMinimas;
    private final int repeticoesMaximas;

    ObjetivoTreino(int seriesMinimas, int seriesMaximas, int repeticoesMinimas, int repeticoesMaximas) {
        this.seriesMinimas = seriesMinimas;
        this.seriesMaximas = seriesMaximas;
        this.repeticoesMinimas = repeticoesMinimas;
        this.repeticoesMaximas = repeticoesMaximas;
    }

    public void validarFaixa(Serie serie) {
        Validate.isTrue(serie.quantidade() >= seriesMinimas && serie.quantidade() <= seriesMaximas,
                PlanoDeTreinoServico.SERIES_FORA_DA_FAIXA_MODELO, name(), seriesMinimas, seriesMaximas);
        Validate.isTrue(serie.repeticoesMinimas() >= repeticoesMinimas
                && serie.repeticoesMaximas() <= repeticoesMaximas,
                PlanoDeTreinoServico.REPETICOES_FORA_DA_FAIXA_MODELO, name(), repeticoesMinimas, repeticoesMaximas);
    }

    public int getSeriesMinimas() { return seriesMinimas; }
    public int getSeriesMaximas() { return seriesMaximas; }
    public int getRepeticoesMinimas() { return repeticoesMinimas; }
    public int getRepeticoesMaximas() { return repeticoesMaximas; }
}
