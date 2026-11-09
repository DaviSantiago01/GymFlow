package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.Exercicio;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

/** Execuções de um exercício, sempre em ordem cronológica. */
public record HistoricoDeCarga(List<ExecucaoDeExercicio> execucoes) implements IObjetoValor {
    public HistoricoDeCarga {
        var ordenadas = new ArrayList<>(execucoes);
        ordenadas.sort(Comparator.comparing(ExecucaoDeExercicio::getData));
        execucoes = List.copyOf(ordenadas);
    }

    public Optional<BigDecimal> maiorCargaValida(Exercicio exercicio) {
        Objects.requireNonNull(exercicio);
        Optional<BigDecimal> maior = Optional.empty();
        for (var execucao : execucoes) {
            var carga = execucao.getCarga();
            if (exercicio.cargaValida(carga) && (maior.isEmpty() || carga.compareTo(maior.get()) > 0)) {
                maior = Optional.of(carga);
            }
        }
        return maior;
    }

    public Optional<BigDecimal> ultimaCargaValida(Exercicio exercicio) {
        Objects.requireNonNull(exercicio);
        for (int indice = execucoes.size() - 1; indice >= 0; indice--) {
            var carga = execucoes.get(indice).getCarga();
            if (exercicio.cargaValida(carga)) {
                return Optional.of(carga);
            }
        }
        return Optional.empty();
    }

    /** Verifica as execuções mais recentes, na quantidade exigida pela academia. */
    public boolean atingiuLimiteConsecutivo(int limiteRepeticoes) {
        int exigidas = ParametrosAcademia.EXECUCOES_PARA_PROGRESSAO;
        if (execucoes.size() < exigidas) {
            return false;
        }
        for (int indice = execucoes.size() - exigidas; indice < execucoes.size(); indice++) {
            if (execucoes.get(indice).getRepeticoes() < limiteRepeticoes) {
                return false;
            }
        }
        return true;
    }

    public Optional<SugestaoProgressao> sugerirProgressao(Exercicio exercicio, int limiteRepeticoes) {
        Objects.requireNonNull(exercicio);
        if (!atingiuLimiteConsecutivo(limiteRepeticoes)) {
            return Optional.empty();
        }
        var ultimaCarga = ultimaCargaValida(exercicio);
        if (ultimaCarga.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(SugestaoProgressao.calcular(ultimaCarga.get(), exercicio));
    }
}
