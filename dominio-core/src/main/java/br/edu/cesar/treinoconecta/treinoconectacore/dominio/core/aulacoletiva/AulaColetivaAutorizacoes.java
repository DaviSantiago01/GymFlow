package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class AulaColetivaAutorizacoes extends AgregadoAutorizacoes<AulaColetiva> {
    private final boolean inscrever;

    public AulaColetivaAutorizacoes(boolean buscar, boolean inscrever) {
        super(false, buscar, false, false);
        this.inscrever = inscrever;
    }

    public boolean getInscrever() { return inscrever; }
}
