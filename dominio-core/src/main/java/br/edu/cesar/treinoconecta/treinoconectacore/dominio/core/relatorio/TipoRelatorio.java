package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

import java.util.EnumSet;
import java.util.Set;

/**
 * Tipos de relatório. Os relatórios nominais listam alunos e podem exibir dados pessoais a quem tem acesso a eles; os
 * demais apresentam apenas dados agregados.
 */
public enum TipoRelatorio {
    DESEMPENHO_GERAL("Desempenho geral", false, false, EnumSet.of(TipoFiltro.PLANO)),
    MATRICULAS_EVASAO("Matrículas e evasão", false, false, EnumSet.of(TipoFiltro.PLANO)),
    FREQUENCIA_OCUPACAO("Frequência e ocupação", false, false,
            EnumSet.of(TipoFiltro.PLANO, TipoFiltro.MODALIDADE, TipoFiltro.AULA_COLETIVA)),
    RECEITA_INADIMPLENCIA("Receita e inadimplência", false, true, EnumSet.of(TipoFiltro.PLANO)),
    DESEMPENHO_PERSONAIS("Desempenho dos personais", false, false, EnumSet.of(TipoFiltro.PERSONAL)),
    RISCO_EVASAO("Alunos em risco de evasão", true, true, EnumSet.of(TipoFiltro.PLANO, TipoFiltro.PERSONAL)),
    ADESAO_TREINOS("Adesão aos treinos", true, false, EnumSet.of(TipoFiltro.PERSONAL)),
    METAS_ACADEMIA("Metas da academia", false, false, EnumSet.noneOf(TipoFiltro.class));

    private final String titulo;
    private final boolean permitidoAoPersonal;
    private final boolean nominal;
    private final Set<TipoFiltro> filtros;

    TipoRelatorio(String titulo, boolean permitidoAoPersonal, boolean nominal, Set<TipoFiltro> filtros) {
        this.titulo = titulo;
        this.permitidoAoPersonal = permitidoAoPersonal;
        this.nominal = nominal;
        this.filtros = filtros;
    }

    public boolean aceita(TipoFiltro filtro) {
        return filtros.contains(filtro);
    }

    public String getTitulo() { return titulo; }
    public boolean isPermitidoAoPersonal() { return permitidoAoPersonal; }
    public boolean isNominal() { return nominal; }
    public Set<TipoFiltro> getFiltros() { return Set.copyOf(filtros); }
}
