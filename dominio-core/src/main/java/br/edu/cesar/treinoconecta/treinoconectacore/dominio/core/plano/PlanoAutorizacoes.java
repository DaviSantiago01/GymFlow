package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class PlanoAutorizacoes extends AgregadoAutorizacoes<Plano> {
    private final boolean cadastrar;
    private final boolean editar;
    private final boolean inativar;

    public PlanoAutorizacoes(boolean buscar, boolean cadastrar, boolean editar, boolean inativar) {
        super(false, buscar, false, false);
        this.cadastrar = cadastrar;
        this.editar = editar;
        this.inativar = inativar;
    }

    public boolean getCadastrar() { return cadastrar; }
    public boolean getEditar() { return editar; }
    public boolean getInativar() { return inativar; }
}
