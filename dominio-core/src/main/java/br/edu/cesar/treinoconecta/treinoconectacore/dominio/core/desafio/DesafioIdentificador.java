package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.desafio;

import dev.sauloaraujo.d5.framework.dominio.IIdentificador;
import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorRegexAbstrato;

public final class DesafioIdentificador extends ObjetoValorRegexAbstrato implements IIdentificador {
    public DesafioIdentificador(String valor) { super(valor,"[1-9][0-9]{0,17}","Identificador de desafio inválido"); }
}
