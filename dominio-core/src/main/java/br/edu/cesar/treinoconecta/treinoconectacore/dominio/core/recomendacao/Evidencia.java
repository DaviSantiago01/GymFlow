package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

/** Dado que fundamenta a recomendação (RN012), como os treinos de uma semana ou a carga de uma execução. */
public record Evidencia(String rotulo, BigDecimal valor, boolean destaque) implements IObjetoValor {
    public static final int TAMANHO_ROTULO = 40;

    public Evidencia {
        Validate.notBlank(rotulo, "Rótulo da evidência obrigatório");
        Validate.isTrue(rotulo.length() <= TAMANHO_ROTULO, "Rótulo da evidência muito longo");
        Validate.notNull(valor, "Valor da evidência obrigatório");
        valor = valor.setScale(2, RoundingMode.HALF_UP);
    }
}
