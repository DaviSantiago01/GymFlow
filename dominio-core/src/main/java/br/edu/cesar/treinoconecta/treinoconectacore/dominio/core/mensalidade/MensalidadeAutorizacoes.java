package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class MensalidadeAutorizacoes extends AgregadoAutorizacoes<Mensalidade> {
    private final boolean registrarPagamento;
    private final boolean estornarPagamento;

    public MensalidadeAutorizacoes(boolean buscar, boolean registrarPagamento, boolean estornarPagamento) {
        super(false, buscar, false, false);
        this.registrarPagamento = registrarPagamento;
        this.estornarPagamento = estornarPagamento;
    }

    public boolean getRegistrarPagamento() { return registrarPagamento; }
    public boolean getEstornarPagamento() { return estornarPagamento; }
}
