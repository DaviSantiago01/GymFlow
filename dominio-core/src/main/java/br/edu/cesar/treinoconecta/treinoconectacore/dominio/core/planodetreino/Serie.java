package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

/** Prescrição das séries de um exercício dentro de um treino. */
public record Serie(ExercicioIdentificador exercicio, int quantidade, int repeticoesMinimas, int repeticoesMaximas,
        BigDecimal carga, int intervaloSegundos) implements IObjetoValor {
    public Serie {
        Objects.requireNonNull(exercicio);
        Objects.requireNonNull(carga);
        Validate.isTrue(quantidade > 0, PlanoDeTreinoServico.SERIES_INVALIDAS);
        Validate.isTrue(repeticoesMinimas > 0 && repeticoesMaximas > 0, PlanoDeTreinoServico.REPETICOES_INVALIDAS);
        Validate.isTrue(repeticoesMaximas >= repeticoesMinimas, PlanoDeTreinoServico.FAIXA_REPETICOES_INVALIDA);
        Validate.isTrue(carga.signum() >= 0, PlanoDeTreinoServico.CARGA_NEGATIVA);
        Validate.isTrue(intervaloSegundos >= ParametrosAcademia.INTERVALO_MINIMO_SEGUNDOS
                && intervaloSegundos <= ParametrosAcademia.INTERVALO_MAXIMO_SEGUNDOS,
                PlanoDeTreinoServico.INTERVALO_FORA_DOS_LIMITES_MODELO,
                ParametrosAcademia.INTERVALO_MINIMO_SEGUNDOS, ParametrosAcademia.INTERVALO_MAXIMO_SEGUNDOS);
        carga = carga.setScale(2, RoundingMode.HALF_UP);
    }
}
