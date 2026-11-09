package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class PlanoDeTreinoAutorizacoes extends AgregadoAutorizacoes<PlanoDeTreino> {
    private final boolean criar;
    private final boolean ativar;
    private final boolean substituir;
    private final boolean revisar;
    private final boolean encerrar;

    public PlanoDeTreinoAutorizacoes(boolean buscar, boolean criar, boolean ativar, boolean substituir,
            boolean revisar, boolean encerrar) {
        super(false, buscar, false, false);
        this.criar = criar;
        this.ativar = ativar;
        this.substituir = substituir;
        this.revisar = revisar;
        this.encerrar = encerrar;
    }

    public boolean getCriar() { return criar; }
    public boolean getAtivar() { return ativar; }
    public boolean getSubstituir() { return substituir; }
    public boolean getRevisar() { return revisar; }
    public boolean getEncerrar() { return encerrar; }
}
