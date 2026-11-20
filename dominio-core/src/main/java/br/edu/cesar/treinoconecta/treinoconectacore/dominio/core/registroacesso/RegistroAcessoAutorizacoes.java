package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

/** A recepção da academia é operada pelo administrador. */
public final class RegistroAcessoAutorizacoes extends AgregadoAutorizacoes<RegistroAcesso> {
    private final boolean registrar;

    public RegistroAcessoAutorizacoes(boolean buscar, boolean registrar) {
        super(false, buscar, false, false);
        this.registrar = registrar;
    }

    public boolean getRegistrar() { return registrar; }
}
