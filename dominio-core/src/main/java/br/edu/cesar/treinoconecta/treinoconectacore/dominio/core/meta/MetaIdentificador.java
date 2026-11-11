package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta;

import dev.sauloaraujo.d5.framework.dominio.IIdentificador;
import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorRegexAbstrato;

public final class MetaIdentificador extends ObjetoValorRegexAbstrato implements IIdentificador {
    public static final int TAMANHO = 18;
    private static final String FORMATO = "[1-9][0-9]{0,17}";
    private static final String MENSAGEM = "Identificador de meta inválido";

    public MetaIdentificador(String valor) {
        super(valor, FORMATO, MENSAGEM);
    }
}
