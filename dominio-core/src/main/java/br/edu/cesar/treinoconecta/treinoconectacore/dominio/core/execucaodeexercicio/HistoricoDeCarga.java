package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
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

    public boolean existeCargaValida(Exercicio exercicio) {
        Objects.requireNonNull(exercicio);
        for (var execucao : execucoes) {
            if (exercicio.cargaValida(execucao.getCarga())) {
                return true;
            }
        }
        return false;
    }

    public BigDecimal buscarMaiorCargaValida(Exercicio exercicio) {
        Objects.requireNonNull(exercicio);
        if (!existeCargaValida(exercicio)) {
            throw new NoSuchElementException(ExecucaoDeExercicioServico.SEM_CARGA_VALIDA);
        }
        var maior = BigDecimal.ZERO;
        for (var execucao : execucoes) {
            var carga = execucao.getCarga();
            if (exercicio.cargaValida(carga) && carga.compareTo(maior) > 0) {
                maior = carga;
            }
        }
        return maior;
    }

    public BigDecimal buscarUltimaCargaValida(Exercicio exercicio) {
        Objects.requireNonNull(exercicio);
        for (int indice = execucoes.size() - 1; indice >= 0; indice--) {
            var carga = execucoes.get(indice).getCarga();
            if (exercicio.cargaValida(carga)) {
                return carga;
            }
        }
        throw new NoSuchElementException(ExecucaoDeExercicioServico.SEM_CARGA_VALIDA);
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
        if (!atingiuLimiteConsecutivo(limiteRepeticoes) || !existeCargaValida(exercicio)) {
            return Optional.empty();
        }
        return Optional.of(SugestaoProgressao.calcular(buscarUltimaCargaValida(exercicio), exercicio));
    }
}
