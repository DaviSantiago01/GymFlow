package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.planodetreino;

import java.math.BigDecimal;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio.ExercicioJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class SerieJpa {
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="EXE_ID") private ExercicioJpa exercicio;
    @Column(name="SRE_QUANTIDADE") private int quantidade;
    @Column(name="SRE_REPETICOES_MINIMAS") private int repeticoesMinimas;
    @Column(name="SRE_REPETICOES_MAXIMAS") private int repeticoesMaximas;
    @NotNull @Column(name="SRE_CARGA") private BigDecimal carga;
    @Column(name="SRE_INTERVALO_SEGUNDOS") private int intervaloSegundos;

    protected SerieJpa() { }

    public SerieJpa(ExercicioJpa exercicio, int quantidade, int repeticoesMinimas, int repeticoesMaximas, BigDecimal carga,
            int intervaloSegundos) {
        this.exercicio = exercicio;
        this.quantidade = quantidade;
        this.repeticoesMinimas = repeticoesMinimas;
        this.repeticoesMaximas = repeticoesMaximas;
        this.carga = carga;
        this.intervaloSegundos = intervaloSegundos;
    }

    public ExercicioJpa getExercicio() { return exercicio; }
    public int getQuantidade() { return quantidade; }
    public int getRepeticoesMinimas() { return repeticoesMinimas; }
    public int getRepeticoesMaximas() { return repeticoesMaximas; }
    public BigDecimal getCarga() { return carga; }
    public int getIntervaloSegundos() { return intervaloSegundos; }
}
