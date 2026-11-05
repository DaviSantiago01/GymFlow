package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario;

import dev.sauloaraujo.d5.framework.dominio.IIdentificador;
import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorStringAbstrato;
import org.apache.commons.lang3.Validate;

public final class ArmarioCodigo extends ObjetoValorStringAbstrato implements IIdentificador {
    public static final int TAMANHO = 5;
    public ArmarioCodigo(String valor) {
        super(valor);
        Validate.matchesPattern(valor, "[A-Z]-[0-9]{2,3}", "Código de armário inválido");
    }
}
