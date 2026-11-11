package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class MetaAutorizacoes extends AgregadoAutorizacoes<Meta> {
    private final boolean criar;
    private final boolean ajustar;
    private final boolean substituir;
    private final boolean encerrar;

    public MetaAutorizacoes(boolean buscar, boolean criar, boolean ajustar, boolean substituir, boolean encerrar) {
        super(false, buscar, false, false);
        this.criar = criar;
        this.ajustar = ajustar;
        this.substituir = substituir;
        this.encerrar = encerrar;
    }

    public boolean getCriar() { return criar; }
    public boolean getAjustar() { return ajustar; }
    public boolean getSubstituir() { return substituir; }
    public boolean getEncerrar() { return encerrar; }
}
