package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class RecomendacaoAutorizacoes extends AgregadoAutorizacoes<Recomendacao> {
    private final boolean gerar;
    private final boolean responder;
    private final boolean aprovar;

    public RecomendacaoAutorizacoes(boolean buscar, boolean gerar, boolean responder, boolean aprovar) {
        super(false, buscar, false, false);
        this.gerar = gerar;
        this.responder = responder;
        this.aprovar = aprovar;
    }

    public boolean getGerar() { return gerar; }
    public boolean getResponder() { return responder; }
    public boolean getAprovar() { return aprovar; }
}
