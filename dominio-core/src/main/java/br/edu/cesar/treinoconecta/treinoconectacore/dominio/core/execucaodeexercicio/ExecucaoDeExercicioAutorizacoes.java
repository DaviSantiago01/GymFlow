package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class ExecucaoDeExercicioAutorizacoes extends AgregadoAutorizacoes<ExecucaoDeExercicio> {
    private final boolean registrar;

    public ExecucaoDeExercicioAutorizacoes(boolean buscar, boolean registrar) {
        super(false, buscar, false, false);
        this.registrar = registrar;
    }

    public boolean getRegistrar() { return registrar; }
}
