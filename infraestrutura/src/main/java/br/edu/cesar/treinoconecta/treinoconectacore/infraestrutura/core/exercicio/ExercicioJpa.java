package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio;

import java.math.BigDecimal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.CategoriaExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.Exercicio;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.jspecify.annotations.Nullable;

@Entity @Table(name="EXE_EXERCICIO")
public class ExercicioJpa implements IAgregadoJpa<Exercicio> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="EXE_ID")
    private @Nullable Long identificador;
    @NotNull @Size(max=Exercicio.TAMANHO_NOME) @Column(name="EXE_NOME",unique=true) private String nome;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="EXE_CATEGORIA") private CategoriaExercicio categoria;
    @NotNull @Column(name="EXE_CARGA_MAXIMA") private BigDecimal cargaMaxima;
    @Column(name="EXE_INCREMENTO_CARGA") private @Nullable BigDecimal incrementoCarga;

    protected ExercicioJpa() { }

    public ExercicioJpa(@Nullable Long identificador, String nome, CategoriaExercicio categoria, BigDecimal cargaMaxima,
            @Nullable BigDecimal incrementoCarga) {
        this.identificador = identificador;
        this.nome = nome;
        this.categoria = categoria;
        this.cargaMaxima = cargaMaxima;
        this.incrementoCarga = incrementoCarga;
    }

    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public String getNome() { return nome; }
    public CategoriaExercicio getCategoria() { return categoria; }
    public BigDecimal getCargaMaxima() { return cargaMaxima; }
    public @Nullable BigDecimal getIncrementoCarga() { return incrementoCarga; }
}
