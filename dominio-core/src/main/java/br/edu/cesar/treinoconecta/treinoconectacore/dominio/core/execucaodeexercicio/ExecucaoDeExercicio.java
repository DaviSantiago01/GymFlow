package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

/** Registro imutável de uma execução: nunca é alterado depois de incluído. */
public final class ExecucaoDeExercicio implements IAgregado<ExecucaoDeExercicioIdentificador> {
    private final Optional<ExecucaoDeExercicioIdentificador> identificador;
    private final AlunoIdentificador aluno;
    private final PlanoDeTreinoIdentificador plano;
    private final DiaSemana dia;
    private final ExercicioIdentificador exercicio;
    private final Instant data;
    private final int series;
    private final int repeticoes;
    private final BigDecimal carga;

    public ExecucaoDeExercicio(Optional<ExecucaoDeExercicioIdentificador> identificador, AlunoIdentificador aluno,
            PlanoDeTreinoIdentificador plano, DiaSemana dia, ExercicioIdentificador exercicio, Instant data, int series,
            int repeticoes, BigDecimal carga) {
        this.identificador = Objects.requireNonNull(identificador);
        this.aluno = Objects.requireNonNull(aluno);
        this.plano = Objects.requireNonNull(plano);
        this.dia = Objects.requireNonNull(dia);
        this.exercicio = Objects.requireNonNull(exercicio);
        this.data = Objects.requireNonNull(data);
        Objects.requireNonNull(carga);
        Validate.isTrue(series > 0, ExecucaoDeExercicioServico.SERIES_INVALIDAS);
        Validate.isTrue(repeticoes > 0, ExecucaoDeExercicioServico.REPETICOES_INVALIDAS);
        Validate.isTrue(carga.signum() >= 0, ExecucaoDeExercicioServico.CARGA_NEGATIVA);
        this.series = series;
        this.repeticoes = repeticoes;
        this.carga = carga.setScale(2, RoundingMode.HALF_UP);
    }

    public static ExecucaoDeExercicio registrar(AlunoIdentificador aluno, PlanoDeTreinoIdentificador plano,
            DiaSemana dia, ExercicioIdentificador exercicio, int series, int repeticoes, BigDecimal carga,
            Instant agora) {
        return new ExecucaoDeExercicio(Optional.empty(), aluno, plano, dia, exercicio, agora, series, repeticoes, carga);
    }

    public Optional<ExecucaoDeExercicioIdentificador> getIdentificador() { return identificador; }
    public AlunoIdentificador getAluno() { return aluno; }
    public PlanoDeTreinoIdentificador getPlano() { return plano; }
    public DiaSemana getDia() { return dia; }
    public ExercicioIdentificador getExercicio() { return exercicio; }
    public Instant getData() { return data; }
    public int getSeries() { return series; }
    public int getRepeticoes() { return repeticoes; }
    public BigDecimal getCarga() { return carga; }
}
