package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.planodetreino;

import java.util.ArrayList;
import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.ObjetivoTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.Treino;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.jspecify.annotations.Nullable;

@Entity @Table(name="TRE_TREINO")
public class TreinoJpa {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="TRE_ID")
    private @Nullable Long identificador;
    @NotNull @Size(max=Treino.TAMANHO_NOME) @Column(name="TRE_NOME") private String nome;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="TRE_DIA") private DiaSemana dia;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="TRE_OBJETIVO") private ObjetivoTreino objetivo;
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="SRE_SERIE", joinColumns=@JoinColumn(name="TRE_ID"))
    @OrderColumn(name="SRE_INDICE")
    private List<SerieJpa> series = new ArrayList<>();

    protected TreinoJpa() { }

    public TreinoJpa(String nome, DiaSemana dia, ObjetivoTreino objetivo, List<SerieJpa> series) {
        this.nome = nome;
        this.dia = dia;
        this.objetivo = objetivo;
        this.series = series;
    }

    public String getNome() { return nome; }
    public DiaSemana getDia() { return dia; }
    public ObjetivoTreino getObjetivo() { return objetivo; }
    public List<SerieJpa> getSeries() { return series; }
}
