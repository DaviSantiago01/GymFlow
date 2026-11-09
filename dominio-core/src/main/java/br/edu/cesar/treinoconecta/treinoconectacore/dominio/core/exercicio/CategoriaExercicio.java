package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;

public enum CategoriaExercicio {
    PEITO(ParametrosAcademia.PERCENTUAL_PROGRESSAO),
    COSTAS(ParametrosAcademia.PERCENTUAL_PROGRESSAO),
    PERNAS(ParametrosAcademia.PERCENTUAL_PROGRESSAO),
    OMBROS(ParametrosAcademia.PERCENTUAL_PROGRESSAO),
    BRACOS(ParametrosAcademia.PERCENTUAL_PROGRESSAO),
    ABDOMEN(ParametrosAcademia.PERCENTUAL_PROGRESSAO);

    private static final BigDecimal CEM = new BigDecimal("100");

    private final BigDecimal percentualProgressao;

    CategoriaExercicio(BigDecimal percentualProgressao) {
        this.percentualProgressao = percentualProgressao;
    }

    public BigDecimal progredirCarga(BigDecimal cargaAtual) {
        var acrescimo = cargaAtual.multiply(percentualProgressao).divide(CEM, 2, RoundingMode.HALF_UP);
        return cargaAtual.add(acrescimo);
    }
}
