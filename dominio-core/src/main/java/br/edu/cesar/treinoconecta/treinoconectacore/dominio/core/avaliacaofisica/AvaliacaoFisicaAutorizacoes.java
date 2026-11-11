package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class AvaliacaoFisicaAutorizacoes extends AgregadoAutorizacoes<AvaliacaoFisica> {
    private final boolean registrar;
    private final boolean corrigir;
    private final boolean remover;

    public AvaliacaoFisicaAutorizacoes(boolean buscar, boolean registrar, boolean corrigir, boolean remover) {
        super(false, buscar, false, false);
        this.registrar = registrar;
        this.corrigir = corrigir;
        this.remover = remover;
    }

    public boolean getRegistrar() { return registrar; }
    public boolean getCorrigir() { return corrigir; }
    public boolean getRemover() { return remover; }
}
