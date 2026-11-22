package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class MetaAcademiaAutorizacoes extends AgregadoAutorizacoes<MetaAcademia> {
    private final boolean cadastrar;
    private final boolean encerrar;
    private final boolean substituir;

    public MetaAcademiaAutorizacoes(boolean buscar, boolean cadastrar, boolean encerrar, boolean substituir) {
        super(false, buscar, false, false);
        this.cadastrar = cadastrar;
        this.encerrar = encerrar;
        this.substituir = substituir;
    }

    public boolean getCadastrar() { return cadastrar; }
    public boolean getEncerrar() { return encerrar; }
    public boolean getSubstituir() { return substituir; }
}
