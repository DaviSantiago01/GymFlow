package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

/** Todas as ações de gerenciamento pertencem ao administrador da academia. */
public final class ProfissionalAutorizacoes extends AgregadoAutorizacoes<Profissional> {
    private final boolean gerenciar;

    public ProfissionalAutorizacoes(boolean buscar, boolean gerenciar) {
        super(false, buscar, false, false);
        this.gerenciar = gerenciar;
    }

    public boolean getCadastrar() { return gerenciar; }
    public boolean getEditar() { return gerenciar; }
    public boolean getGerenciarEspecialidades() { return gerenciar; }
    public boolean getGerenciarDisponibilidades() { return gerenciar; }
    public boolean getInativar() { return gerenciar; }
}
