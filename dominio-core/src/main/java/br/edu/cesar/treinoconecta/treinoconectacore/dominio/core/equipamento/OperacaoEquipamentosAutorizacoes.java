package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento;

import dev.sauloaraujo.d5.framework.dominio.IAgregado;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class OperacaoEquipamentosAutorizacoes<A extends IAgregado<?>> extends AgregadoAutorizacoes<A> {
    private final boolean operar;

    public OperacaoEquipamentosAutorizacoes(boolean administrador, boolean excluir) {
        super(false, administrador, false, administrador && excluir);
        this.operar = administrador;
    }

    public boolean getOperar() {
        return operar;
    }
}
