package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento;

import dev.sauloaraujo.d5.framework.dominio.IIdentificador;
import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorRegexAbstrato;

public final class EquipamentoIdentificador extends ObjetoValorRegexAbstrato implements IIdentificador {
    public EquipamentoIdentificador(String valor) {
        super(valor, "[1-9][0-9]{0,17}", "Identificador inválido");
    }
}
