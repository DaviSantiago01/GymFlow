package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aula;

import dev.sauloaraujo.d5.framework.dominio.IIdentificador;
import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorRegexAbstrato;

public final class AulaIdentificador extends ObjetoValorRegexAbstrato implements IIdentificador {
    public AulaIdentificador(String valor) { super(valor, "[1-9][0-9]{0,17}", "Identificador de aula inválido"); }
}
