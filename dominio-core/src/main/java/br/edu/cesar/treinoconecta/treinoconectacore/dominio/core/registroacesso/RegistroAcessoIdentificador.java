package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso;

import dev.sauloaraujo.d5.framework.dominio.IIdentificador;
import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorRegexAbstrato;

public final class RegistroAcessoIdentificador extends ObjetoValorRegexAbstrato implements IIdentificador {
    private static final String FORMATO = "[1-9][0-9]{0,17}";
    private static final String MENSAGEM = "Identificador de registro de acesso inválido";

    public RegistroAcessoIdentificador(String valor) {
        super(valor, FORMATO, MENSAGEM);
    }
}
