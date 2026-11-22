package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

import java.util.List;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class RelatorioAutorizacoes extends AgregadoAutorizacoes<Relatorio> {
    private final boolean gerar;
    private final boolean exportar;
    private final boolean gerarNovaVersao;
    private final boolean painel;
    private final List<TipoRelatorio> tipos;

    public RelatorioAutorizacoes(boolean buscar, boolean gerar, boolean exportar, boolean gerarNovaVersao,
            boolean painel, List<TipoRelatorio> tipos) {
        super(false, buscar, false, false);
        this.gerar = gerar;
        this.exportar = exportar;
        this.gerarNovaVersao = gerarNovaVersao;
        this.painel = painel;
        this.tipos = List.copyOf(tipos);
    }

    public boolean getGerar() { return gerar; }
    public boolean getExportar() { return exportar; }
    public boolean getGerarNovaVersao() { return gerarNovaVersao; }
    public boolean getPainel() { return painel; }
    public List<TipoRelatorio> getTipos() { return tipos; }
}
