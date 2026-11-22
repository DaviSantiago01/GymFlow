package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aula;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class AulaAutorizacoes extends AgregadoAutorizacoes<Aula> {
    private final boolean criar;
    public AulaAutorizacoes(boolean buscar, boolean criar, boolean alterar, boolean excluir) {
        super(criar, buscar, alterar, excluir); this.criar=criar;
    }
    public boolean getCriar() { return criar; }
}
