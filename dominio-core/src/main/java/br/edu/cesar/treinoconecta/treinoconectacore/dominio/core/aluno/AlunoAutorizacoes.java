package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

/** A consulta segue as regras anteriores (personal ou o próprio aluno); o gerenciamento cabe ao administrador. */
public final class AlunoAutorizacoes extends AgregadoAutorizacoes<Aluno> {
    private final boolean gerenciar;

    public AlunoAutorizacoes(boolean buscar, boolean gerenciar) {
        super(false, buscar, false, false);
        this.gerenciar = gerenciar;
    }

    public boolean getGerenciar() { return gerenciar; }
}
