package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento;

import dev.sauloaraujo.d5.framework.dominio.IIdentificador;
import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorRegexAbstrato;

public final class RegistroEquipamentoIdentificador extends ObjetoValorRegexAbstrato implements IIdentificador {
    public RegistroEquipamentoIdentificador(String valor) {
        super(valor, "[1-9][0-9]{0,17}", "Identificador inválido");
    }
}
