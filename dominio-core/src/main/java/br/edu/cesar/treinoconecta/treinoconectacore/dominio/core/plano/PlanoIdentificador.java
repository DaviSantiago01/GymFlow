package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano;

import dev.sauloaraujo.d5.framework.dominio.IIdentificador;
import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorRegexAbstrato;

public final class PlanoIdentificador extends ObjetoValorRegexAbstrato implements IIdentificador {
    public static final int TAMANHO = 18;
    private static final String FORMATO = "[1-9][0-9]{0,17}";
    private static final String MENSAGEM = "Identificador de plano inválido";

    public PlanoIdentificador(String valor) {
        super(valor, FORMATO, MENSAGEM);
    }
}
