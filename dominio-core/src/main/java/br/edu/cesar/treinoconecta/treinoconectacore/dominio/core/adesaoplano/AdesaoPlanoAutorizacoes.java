package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class AdesaoPlanoAutorizacoes extends AgregadoAutorizacoes<AdesaoPlano> {
    private final boolean aderir;
    private final boolean trocar;
    private final boolean cancelar;
    private final boolean gerarMensalidades;

    public AdesaoPlanoAutorizacoes(boolean buscar, boolean aderir, boolean trocar, boolean cancelar,
            boolean gerarMensalidades) {
        super(false, buscar, false, false);
        this.aderir = aderir;
        this.trocar = trocar;
        this.cancelar = cancelar;
        this.gerarMensalidades = gerarMensalidades;
    }

    public boolean getAderir() { return aderir; }
    public boolean getTrocar() { return trocar; }
    public boolean getCancelar() { return cancelar; }
    public boolean getGerarMensalidades() { return gerarMensalidades; }
}
