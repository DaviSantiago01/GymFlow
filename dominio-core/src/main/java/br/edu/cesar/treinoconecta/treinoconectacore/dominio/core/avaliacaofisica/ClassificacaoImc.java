package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica;

import java.math.BigDecimal;
import java.util.Objects;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;

public enum ClassificacaoImc {
    ABAIXO_DO_PESO, PESO_NORMAL, SOBREPESO, OBESIDADE_GRAU_I, OBESIDADE_GRAU_II, OBESIDADE_GRAU_III;

    public static ClassificacaoImc classificar(BigDecimal imc) {
        Objects.requireNonNull(imc);
        if (imc.compareTo(ParametrosAcademia.IMC_LIMITE_ABAIXO_DO_PESO) < 0) {
            return ABAIXO_DO_PESO;
        }
        if (imc.compareTo(ParametrosAcademia.IMC_LIMITE_NORMAL) < 0) {
            return PESO_NORMAL;
        }
        if (imc.compareTo(ParametrosAcademia.IMC_LIMITE_SOBREPESO) < 0) {
            return SOBREPESO;
        }
        if (imc.compareTo(ParametrosAcademia.IMC_LIMITE_OBESIDADE_GRAU_I) < 0) {
            return OBESIDADE_GRAU_I;
        }
        if (imc.compareTo(ParametrosAcademia.IMC_LIMITE_OBESIDADE_GRAU_II) < 0) {
            return OBESIDADE_GRAU_II;
        }
        return OBESIDADE_GRAU_III;
    }
}
