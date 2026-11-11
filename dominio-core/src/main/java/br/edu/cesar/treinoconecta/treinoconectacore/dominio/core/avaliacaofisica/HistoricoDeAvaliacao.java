package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
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

    public boolean existeAvaliacao() {
        return !avaliacoes.isEmpty();
    }

    public AvaliacaoFisica buscarMaisRecente() {
        if (avaliacoes.isEmpty()) {
            throw new NoSuchElementException(AvaliacaoFisicaServico.SEM_AVALIACAO);
        }
        return avaliacoes.get(avaliacoes.size() - 1);
    }

    public boolean existeAvaliacaoCom(IndicadorFisico indicador) {
        Objects.requireNonNull(indicador);
        for (var avaliacao : avaliacoes) {
            if (avaliacao.existeValor(indicador)) {
                return true;
            }
        }
        return false;
    }

    public AvaliacaoFisica buscarMaisRecenteCom(IndicadorFisico indicador) {
        Objects.requireNonNull(indicador);
        for (int indice = avaliacoes.size() - 1; indice >= 0; indice--) {
            if (avaliacoes.get(indice).existeValor(indicador)) {
                return avaliacoes.get(indice);
            }
        }
        throw new NoSuchElementException(String.format(AvaliacaoFisicaServico.SEM_AVALIACAO_DO_INDICADOR_MODELO, indicador));
    }

    public boolean isMaisRecente(AvaliacaoFisica avaliacao) {
        Objects.requireNonNull(avaliacao);
        return existeAvaliacao() && buscarMaisRecente().getIdentificador().equals(avaliacao.getIdentificador());
    }

    public boolean existeAnteriorA(AvaliacaoFisica atual) {
        Objects.requireNonNull(atual);
        for (var avaliacao : avaliacoes) {
            if (avaliacao.getData().isBefore(atual.getData())) {
                return true;
            }
        }
        return false;
    }

    /** A lista está em ordem cronológica: a última antes da data é a imediatamente anterior. */
    public AvaliacaoFisica buscarAnteriorA(AvaliacaoFisica atual) {
        Objects.requireNonNull(atual);
        if (!existeAnteriorA(atual)) {
            throw new NoSuchElementException(AvaliacaoFisicaServico.SEM_AVALIACAO_ANTERIOR);
        }
        var anterior = avaliacoes.get(0);
        for (var avaliacao : avaliacoes) {
            if (avaliacao.getData().isBefore(atual.getData())) {
                anterior = avaliacao;
            }
        }
        return anterior;
    }

    public List<PontoIndicador> pontos(IndicadorFisico indicador) {
        Objects.requireNonNull(indicador);
        var pontos = new ArrayList<PontoIndicador>();
        for (var avaliacao : avaliacoes) {
            if (avaliacao.existeValor(indicador)) {
                pontos.add(new PontoIndicador(avaliacao.getData(), avaliacao.buscarValor(indicador)));
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
        if (existeAvaliacao()) {
            var recente = buscarMaisRecente();
            Validate.isTrue(data.isAfter(recente.getData()), AvaliacaoFisicaServico.DATA_FORA_DE_ORDEM_MODELO,
                    recente.getData());
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
