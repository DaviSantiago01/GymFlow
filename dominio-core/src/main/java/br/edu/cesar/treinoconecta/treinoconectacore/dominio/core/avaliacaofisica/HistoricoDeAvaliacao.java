package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

/** Avaliações válidas de um aluno, sempre em ordem cronológica. */
public record HistoricoDeAvaliacao(List<AvaliacaoFisica> avaliacoes) implements IObjetoValor {
    public HistoricoDeAvaliacao {
        var validas = new ArrayList<AvaliacaoFisica>();
        for (var avaliacao : avaliacoes) {
            if (avaliacao.isValida()) {
                validas.add(avaliacao);
            }
        }
        validas.sort(Comparator.comparing(AvaliacaoFisica::getData));
        avaliacoes = List.copyOf(validas);
    }

    public Optional<AvaliacaoFisica> maisRecente() {
        if (avaliacoes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(avaliacoes.get(avaliacoes.size() - 1));
    }

    public Optional<AvaliacaoFisica> maisRecenteCom(IndicadorFisico indicador) {
        Objects.requireNonNull(indicador);
        for (int indice = avaliacoes.size() - 1; indice >= 0; indice--) {
            if (avaliacoes.get(indice).valorDe(indicador).isPresent()) {
                return Optional.of(avaliacoes.get(indice));
            }
        }
        return Optional.empty();
    }

    public boolean isMaisRecente(AvaliacaoFisica avaliacao) {
        Objects.requireNonNull(avaliacao);
        var recente = maisRecente();
        return recente.isPresent() && recente.get().getIdentificador().equals(avaliacao.getIdentificador());
    }

    public Optional<AvaliacaoFisica> anteriorA(AvaliacaoFisica atual) {
        Objects.requireNonNull(atual);
        Optional<AvaliacaoFisica> anterior = Optional.empty();
        for (var avaliacao : avaliacoes) {
            if (avaliacao.getData().isBefore(atual.getData())) {
                anterior = Optional.of(avaliacao);
            }
        }
        return anterior;
    }

    public List<PontoIndicador> pontos(IndicadorFisico indicador) {
        Objects.requireNonNull(indicador);
        var pontos = new ArrayList<PontoIndicador>();
        for (var avaliacao : avaliacoes) {
            var valor = avaliacao.valorDe(indicador);
            if (valor.isPresent()) {
                pontos.add(new PontoIndicador(avaliacao.getData(), valor.get()));
            }
        }
        return pontos;
    }

    /** Variação entre o valor atual e o valor mais antigo das avaliações anteriores exigidas. */
    public Optional<BigDecimal> variacaoRecente(IndicadorFisico indicador) {
        var pontos = pontos(indicador);
        int anteriores = ParametrosAcademia.AVALIACOES_ANTERIORES_PARA_TENDENCIA;
        if (pontos.size() < anteriores + 1) {
            return Optional.empty();
        }
        var atual = pontos.get(pontos.size() - 1).valor();
        var base = pontos.get(pontos.size() - 1 - anteriores).valor();
        return Optional.of(ComparacaoIndicador.variacaoPercentual(base, atual));
    }

    public Optional<TendenciaIndicador> tendencia(IndicadorFisico indicador) {
        var variacao = variacaoRecente(indicador);
        if (variacao.isEmpty()) {
            return Optional.empty();
        }
        if (variacao.get().abs().compareTo(ParametrosAcademia.MARGEM_ESTABILIDADE_PERCENTUAL) <= 0) {
            return Optional.of(TendenciaIndicador.ESTAVEL);
        }
        return Optional.of(variacao.get().signum() > 0 ? TendenciaIndicador.AUMENTANDO : TendenciaIndicador.DIMINUINDO);
    }

    public Optional<InterpretacaoEvolucao> interpretacao(IndicadorFisico indicador) {
        var variacao = variacaoRecente(indicador);
        if (variacao.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(indicador.interpretar(variacao.get()));
    }

    public void validarNovaData(LocalDate data) {
        Objects.requireNonNull(data);
        var recente = maisRecente();
        if (recente.isPresent()) {
            Validate.isTrue(data.isAfter(recente.get().getData()), AvaliacaoFisicaServico.DATA_FORA_DE_ORDEM_MODELO,
                    recente.get().getData());
        }
    }

    public void validarDataCorrigida(AvaliacaoFisica original, LocalDate novaData) {
        Objects.requireNonNull(original);
        Objects.requireNonNull(novaData);
        for (var avaliacao : avaliacoes) {
            if (!avaliacao.getIdentificador().equals(original.getIdentificador())) {
                boolean antesDaOriginal = avaliacao.getData().isBefore(original.getData());
                boolean ordemMantida = antesDaOriginal ? avaliacao.getData().isBefore(novaData)
                        : avaliacao.getData().isAfter(novaData);
                Validate.isTrue(ordemMantida, AvaliacaoFisicaServico.DATA_CORRIGIDA_FORA_DE_ORDEM);
            }
        }
    }
}
