package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class PerfilAlunoAutorizacoes extends AgregadoAutorizacoes<PerfilAluno> {
    private final boolean editar;

    public PerfilAlunoAutorizacoes(boolean buscar, boolean editar) {
        super(false, buscar, false, false);
        this.editar = editar;
    }

    public boolean getEditar() { return editar; }
}
