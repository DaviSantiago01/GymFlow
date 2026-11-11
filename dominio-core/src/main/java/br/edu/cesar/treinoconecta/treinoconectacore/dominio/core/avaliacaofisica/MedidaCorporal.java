package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

public record MedidaCorporal(IndicadorFisico indicador, BigDecimal valor) implements IObjetoValor {
    public MedidaCorporal {
        Objects.requireNonNull(indicador);
        Objects.requireNonNull(valor);
        Validate.isTrue(indicador.isMedidaCorporal(), AvaliacaoFisicaServico.INDICADOR_NAO_E_MEDIDA_MODELO, indicador);
        Validate.isTrue(valor.compareTo(ParametrosAcademia.MEDIDA_MINIMA) >= 0
                && valor.compareTo(ParametrosAcademia.MEDIDA_MAXIMA) <= 0,
                AvaliacaoFisicaServico.MEDIDA_FORA_DOS_LIMITES_MODELO, indicador, ParametrosAcademia.MEDIDA_MINIMA,
                ParametrosAcademia.MEDIDA_MAXIMA);
        valor = valor.setScale(2, RoundingMode.HALF_UP);
    }
}
