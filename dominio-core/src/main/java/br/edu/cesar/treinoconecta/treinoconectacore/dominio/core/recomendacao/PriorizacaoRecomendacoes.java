package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;

/**
 * RN016: ordena as recomendações visíveis por categoria e, no empate, da mais recente para a mais antiga, limitando a
 * quantidade exibida. RN018: categorias com taxa de aceitação baixa nas últimas respostas do aluno perdem prioridade.
 */
public final class PriorizacaoRecomendacoes {
    private static final BigDecimal CEM = new BigDecimal("100");

    private final Map<CategoriaRecomendacao, TaxaAceitacao> taxas;

    public PriorizacaoRecomendacoes(List<Recomendacao> historico) {
        Objects.requireNonNull(historico);
        taxas = new EnumMap<>(CategoriaRecomendacao.class);
        var respondidas = new ArrayList<>(historico.stream().filter(PriorizacaoRecomendacoes::respondidaPeloAluno).toList());
        respondidas.sort(Comparator.comparing(PriorizacaoRecomendacoes::dataDaResposta).reversed());
        for (var categoria : CategoriaRecomendacao.values()) {
            var ultimas = respondidas.stream().filter(recomendacao -> recomendacao.getCategoria() == categoria)
                    .limit(ParametrosRecomendacao.RESPOSTAS_CONSIDERADAS).toList();
            int aceitas = (int) ultimas.stream().filter(recomendacao -> recomendacao.getSituacao().aceita()).count();
            var percentual = ultimas.isEmpty() ? BigDecimal.ZERO
                    : BigDecimal.valueOf(aceitas).multiply(CEM).divide(BigDecimal.valueOf(ultimas.size()), 0,
                            RoundingMode.HALF_UP);
            boolean reduzida = categoria.ajustavelPelasRespostas() && !ultimas.isEmpty()
                    && percentual.compareTo(ParametrosRecomendacao.TAXA_ACEITACAO_MINIMA) < 0;
            var desde = reduzida ? ultimas.stream().filter(recomendacao -> !recomendacao.getSituacao().aceita())
                    .map(PriorizacaoRecomendacoes::dataDaResposta).min(Comparator.naturalOrder()) : Optional.<LocalDate>empty();
            taxas.put(categoria, new TaxaAceitacao(categoria, aceitas, ultimas.size(), percentual, reduzida, desde));
        }
    }

    /** Respostas do próprio aluno: aceite, recusa ou ausência de resposta até a validade. */
    private static boolean respondidaPeloAluno(Recomendacao recomendacao) {
        if (recomendacao.getCriterio().isExigeAprovacao()) {
            return recomendacao.getSituacao() == SituacaoRecomendacao.APROVADA
                    || recomendacao.getSituacao() == SituacaoRecomendacao.REJEITADA;
        }
        var situacao = recomendacao.getSituacao();
        return situacao == SituacaoRecomendacao.ACEITA || situacao == SituacaoRecomendacao.RECUSADA
                || situacao == SituacaoRecomendacao.EXPIRADA;
    }

    private static LocalDate dataDaResposta(Recomendacao recomendacao) {
        return recomendacao.getResposta()
                .map(resposta -> resposta.instante().atZone(ParametrosAcademia.FUSO).toLocalDate())
                .orElse(recomendacao.getValidade());
    }

    public int prioridade(CategoriaRecomendacao categoria) {
        return categoria.getPrioridade()
                + (Objects.requireNonNull(taxas.get(categoria)).prioridadeReduzida() ? ParametrosRecomendacao.PENALIDADE_PRIORIDADE : 0);
    }

    public List<Recomendacao> ordenar(List<Recomendacao> visiveis) {
        var ordenadas = new ArrayList<>(visiveis);
        ordenadas.sort(Comparator.<Recomendacao>comparingInt(recomendacao -> prioridade(recomendacao.getCategoria()))
                .thenComparing(Comparator.comparing(Recomendacao::getGeracao).reversed()));
        return ordenadas.subList(0, Math.min(ordenadas.size(), ParametrosRecomendacao.MAXIMO_EXIBIDAS));
    }

    public List<TaxaAceitacao> taxas() {
        return List.copyOf(taxas.values());
    }

    public record TaxaAceitacao(CategoriaRecomendacao categoria, int aceitas, int respondidas, BigDecimal percentual,
            boolean prioridadeReduzida, Optional<LocalDate> reduzidaDesde) {
    }
}
