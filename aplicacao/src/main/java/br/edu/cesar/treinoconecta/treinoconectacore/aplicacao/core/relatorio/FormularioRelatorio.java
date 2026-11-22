package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio;

import java.time.LocalDate;
import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.TipoRelatorio;

/** Opções da tela de novo relatório: tipos permitidos ao usuário, filtros cadastrados e limites de período. */
public record FormularioRelatorio(List<TipoRelatorio> tipos, List<OpcaoFiltro> planos, List<OpcaoFiltro> personais,
        List<OpcaoFiltro> aulas, List<String> modalidades, LocalDate hoje, int periodoMaximoDias,
        int agrupamentoDiarioMaximoDias, boolean personalRestrito) {
}
