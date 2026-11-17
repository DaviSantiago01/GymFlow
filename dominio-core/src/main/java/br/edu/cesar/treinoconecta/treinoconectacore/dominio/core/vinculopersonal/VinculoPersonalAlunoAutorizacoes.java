package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class VinculoPersonalAlunoAutorizacoes extends AgregadoAutorizacoes<VinculoPersonalAluno> {
    private final boolean vincular;
    private final boolean encerrar;

    public VinculoPersonalAlunoAutorizacoes(boolean buscar, boolean vincular, boolean encerrar) {
        super(false, buscar, false, false);
        this.vincular = vincular;
        this.encerrar = encerrar;
    }

    public boolean getVincular() { return vincular; }
    public boolean getEncerrar() { return encerrar; }
}
