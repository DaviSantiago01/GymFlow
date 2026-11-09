package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.execucaodeexercicio;

import java.math.BigDecimal;
import java.time.Instant;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio.ExercicioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.planodetreino.PlanoDeTreinoJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

@Entity @Table(name="EXC_EXECUCAO_EXERCICIO")
public class ExecucaoDeExercicioJpa implements IAgregadoJpa<ExecucaoDeExercicio> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="EXC_ID")
    private @Nullable Long identificador;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ALU_ID") private AlunoJpa aluno;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="PLT_ID") private PlanoDeTreinoJpa plano;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="EXE_ID") private ExercicioJpa exercicio;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="EXC_DIA") private DiaSemana dia;
    @NotNull @Column(name="EXC_DATA") private Instant data;
    @Column(name="EXC_SERIES") private int series;
    @Column(name="EXC_REPETICOES") private int repeticoes;
    @NotNull @Column(name="EXC_CARGA") private BigDecimal carga;

    protected ExecucaoDeExercicioJpa() { }

    public ExecucaoDeExercicioJpa(@Nullable Long identificador, AlunoJpa aluno, PlanoDeTreinoJpa plano,
            ExercicioJpa exercicio, DiaSemana dia, Instant data, int series, int repeticoes, BigDecimal carga) {
        this.identificador = identificador;
        this.aluno = aluno;
        this.plano = plano;
        this.exercicio = exercicio;
        this.dia = dia;
        this.data = data;
        this.series = series;
        this.repeticoes = repeticoes;
        this.carga = carga;
    }

    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public AlunoJpa getAluno() { return aluno; }
    public PlanoDeTreinoJpa getPlano() { return plano; }
    public ExercicioJpa getExercicio() { return exercicio; }
    public DiaSemana getDia() { return dia; }
    public Instant getData() { return data; }
    public int getSeries() { return series; }
    public int getRepeticoes() { return repeticoes; }
    public BigDecimal getCarga() { return carga; }
}
