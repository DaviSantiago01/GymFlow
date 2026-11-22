package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.MetaAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.CumprimentoMeta;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.ValorIndicador;

/** Monta o conteúdo de cada tipo de relatório a partir da análise dos registros. */
public final class GeradorConteudo {
    private final AnaliseAcademia analise;
    private final PeriodoAnalise periodo;
    private final boolean comparar;
    private final boolean anteriorComDados;

    public GeradorConteudo(AnaliseAcademia analise, PeriodoAnalise periodo, boolean comparar) {
        this.analise = Objects.requireNonNull(analise);
        this.periodo = Objects.requireNonNull(periodo);
        this.comparar = comparar;
        this.anteriorComDados = comparar && analise.possuiDados(periodo.anterior());
    }

    public ConteudoRelatorio gerar(TipoRelatorio tipo, Agrupamento agrupamento, List<MetaAcademia> metas) {
        Objects.requireNonNull(tipo);
        Objects.requireNonNull(agrupamento);
        var resumo = analise.resumo(periodo);
        var indicadores = new ArrayList<ValorIndicador>();
        var grupos = List.<ConteudoRelatorio.ResultadoGrupo>of();
        Optional<ConteudoRelatorio.MovimentoAlunos> movimento = Optional.empty();
        var planos = List.<ConteudoRelatorio.ResultadoPlano>of();
        Optional<ConteudoRelatorio.Ocupacao> ocupacao = Optional.empty();
        Optional<ConteudoRelatorio.Financeiro> financeiro = Optional.empty();
        var personais = List.<ConteudoRelatorio.DesempenhoPersonal>of();
        var emRisco = List.<ConteudoRelatorio.AlunoEmRisco>of();
        var cumprimentos = List.<CumprimentoMeta>of();
        switch (tipo) {
        case DESEMPENHO_GERAL -> {
            for (var indicador : IndicadorAcademia.values()) {
                indicadores.add(indicador(indicador));
            }
            grupos = analise.grupos(periodo, agrupamento);
            movimento = Optional.of(analise.movimento(periodo));
            planos = analise.resultadosPorPlano(periodo);
        }
        case MATRICULAS_EVASAO -> {
            for (var indicador : List.of(IndicadorAcademia.ALUNOS_ATIVOS, IndicadorAcademia.NOVAS_MATRICULAS,
                    IndicadorAcademia.REATIVACOES, IndicadorAcademia.TAXA_EVASAO, IndicadorAcademia.TAXA_RENOVACAO)) {
                indicadores.add(indicador(indicador));
            }
            grupos = analise.grupos(periodo, agrupamento);
            movimento = Optional.of(analise.movimento(periodo));
            planos = analise.resultadosPorPlano(periodo);
        }
        case FREQUENCIA_OCUPACAO -> {
            indicadores.add(indicador(IndicadorAcademia.FREQUENCIA_MEDIA));
            grupos = analise.grupos(periodo, agrupamento);
            ocupacao = Optional.of(analise.ocupacao(periodo));
        }
        case RECEITA_INADIMPLENCIA -> {
            indicadores.add(indicador(IndicadorAcademia.RECEITA_LIQUIDA));
            indicadores.add(indicador(IndicadorAcademia.TAXA_INADIMPLENCIA));
            grupos = analise.grupos(periodo, agrupamento);
            financeiro = Optional.of(analise.financeiro(periodo));
            planos = analise.resultadosPorPlano(periodo);
        }
        case DESEMPENHO_PERSONAIS, ADESAO_TREINOS -> {
            emRisco = analise.alunosEmRisco(periodo);
            personais = analise.personais(periodo, emRisco);
            emRisco = List.of();
        }
        case RISCO_EVASAO -> {
            indicadores.add(indicador(IndicadorAcademia.ALUNOS_EM_RISCO));
            emRisco = analise.alunosEmRisco(periodo);
        }
        case METAS_ACADEMIA -> {
            var lista = new ArrayList<CumprimentoMeta>();
            for (var meta : metas) {
                var apurado = meta.periodoApuradoAte(analise.getHoje());
                if (apurado.isEmpty()) {
                    continue;
                }
                var realizado = analise.valor(meta.getIndicador(), apurado.get());
                lista.add(meta.cumprimento(realizado));
                indicadores.add(new ValorIndicador(meta.getIndicador(), realizado, Optional.empty(), Optional.empty()));
            }
            cumprimentos = lista;
        }
        }
        return new ConteudoRelatorio(resumo, indicadores, grupos, movimento, planos, ocupacao, financeiro, personais,
                emRisco, cumprimentos);
    }

    private ValorIndicador indicador(IndicadorAcademia indicador) {
        var atual = analise.valor(indicador, periodo);
        if (!comparar) {
            return new ValorIndicador(indicador, atual, Optional.empty(), Optional.empty());
        }
        var anterior = anteriorComDados ? Optional.of(analise.valor(indicador, periodo.anterior())) : Optional.<BigDecimal>empty();
        return new ValorIndicador(indicador, atual, anterior,
                Optional.of(Variacao.calcular(atual, anterior, indicador.getSentido())));
    }
}
