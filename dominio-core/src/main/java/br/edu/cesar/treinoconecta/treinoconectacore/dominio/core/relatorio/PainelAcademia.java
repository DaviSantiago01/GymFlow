package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

import java.util.List;
import java.util.Objects;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.CumprimentoMeta;

/** Indicadores do mês, comparados ao período anterior equivalente, e a evolução dos alunos ativos. */
public record PainelAcademia(PeriodoAnalise periodo, PeriodoAnalise anterior, ConteudoRelatorio conteudo,
        List<PontoSerie> alunosAtivos, List<CumprimentoMeta> metas) {
    public PainelAcademia {
        Objects.requireNonNull(periodo);
        Objects.requireNonNull(anterior);
        Objects.requireNonNull(conteudo);
        alunosAtivos = List.copyOf(alunosAtivos);
        metas = List.copyOf(metas);
    }

    /** Alunos ativos no último dia do mês informado (AAAA-MM), ou hoje no mês corrente. */
    public record PontoSerie(String mes, int valor) {
    }
}
