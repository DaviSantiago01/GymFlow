package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.desafio;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class DesafioAutorizacoes extends AgregadoAutorizacoes<Desafio> {
    private final boolean criar;
    public DesafioAutorizacoes(boolean buscar,boolean criar,boolean alterar,boolean excluir) {
        super(criar,buscar,alterar,excluir); this.criar=criar;
    }
    public boolean getCriar() { return criar; }
}
