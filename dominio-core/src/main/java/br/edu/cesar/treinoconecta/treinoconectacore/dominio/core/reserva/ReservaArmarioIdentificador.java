package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva;

import dev.sauloaraujo.d5.framework.dominio.IIdentificador;
import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorStringAbstrato;
import org.apache.commons.lang3.Validate;

public final class ReservaArmarioIdentificador extends ObjetoValorStringAbstrato implements IIdentificador {
    public static final int TAMANHO = 18;
    public ReservaArmarioIdentificador(String valor) {
        super(valor);
        Validate.matchesPattern(valor, "[1-9][0-9]{0,17}", "Identificador inválido");
    }
}
