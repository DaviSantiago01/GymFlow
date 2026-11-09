package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta;

import java.math.BigDecimal;
import java.util.Objects;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;

public enum StatusCumprimento {
    DISTANTE, PROXIMA, ATINGIDA;

    private static final BigDecimal CEM = new BigDecimal("100");

    public static StatusCumprimento classificar(BigDecimal percentual) {
        Objects.requireNonNull(percentual);
        if (percentual.compareTo(CEM) >= 0) {
            return ATINGIDA;
        }
        if (percentual.compareTo(ParametrosAcademia.PERCENTUAL_META_PROXIMA) >= 0) {
            return PROXIMA;
        }
        return DISTANTE;
    }
}
