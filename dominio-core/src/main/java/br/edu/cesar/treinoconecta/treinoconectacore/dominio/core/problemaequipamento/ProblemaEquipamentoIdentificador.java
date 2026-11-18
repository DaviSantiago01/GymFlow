package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento;

import dev.sauloaraujo.d5.framework.dominio.IIdentificador;
import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorRegexAbstrato;

public final class ProblemaEquipamentoIdentificador extends ObjetoValorRegexAbstrato implements IIdentificador {
    public ProblemaEquipamentoIdentificador(String valor) {
        super(valor, "[1-9][0-9]{0,17}", "Identificador inválido");
    }
}
