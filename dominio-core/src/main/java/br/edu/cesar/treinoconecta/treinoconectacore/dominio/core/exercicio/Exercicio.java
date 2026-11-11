package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

public final class Exercicio implements IAgregado<ExercicioIdentificador> {
    public static final int TAMANHO_NOME = 100;

    private final Optional<ExercicioIdentificador> identificador;
    private final String nome;
    private final CategoriaExercicio categoria;
    private final BigDecimal cargaMaxima;
    private final Optional<BigDecimal> incrementoCarga;

    public Exercicio(Optional<ExercicioIdentificador> identificador, String nome, CategoriaExercicio categoria,
            BigDecimal cargaMaxima, Optional<BigDecimal> incrementoCarga) {
        this.identificador = Objects.requireNonNull(identificador);
        this.nome = Validate.notBlank(nome, ExercicioServico.NOME_INVALIDO);
        this.categoria = Objects.requireNonNull(categoria);
        this.cargaMaxima = Objects.requireNonNull(cargaMaxima);
        this.incrementoCarga = Objects.requireNonNull(incrementoCarga);
        Validate.isTrue(nome.length() <= TAMANHO_NOME, ExercicioServico.NOME_INVALIDO);
        Validate.isTrue(cargaMaxima.signum() > 0, ExercicioServico.CARGA_MAXIMA_INVALIDA);
        if (incrementoCarga.isPresent()) {
            Validate.isTrue(incrementoCarga.get().signum() > 0, ExercicioServico.INCREMENTO_INVALIDO);
        }
    }

    public boolean cargaValida(BigDecimal carga) {
        Objects.requireNonNull(carga);
        return carga.signum() > 0 && carga.compareTo(cargaMaxima) <= 0;
    }

    /** Aplica o incremento do exercício ou, se ele não tiver, o percentual da categoria. */
    public BigDecimal progredirCarga(BigDecimal cargaAtual) {
        Objects.requireNonNull(cargaAtual);
        if (incrementoCarga.isPresent()) {
            return cargaAtual.add(incrementoCarga.get());
        }
        return categoria.progredirCarga(cargaAtual);
    }

    public Optional<ExercicioIdentificador> getIdentificador() { return identificador; }
    public String getNome() { return nome; }
    public CategoriaExercicio getCategoria() { return categoria; }
    public BigDecimal getCargaMaxima() { return cargaMaxima; }
    public Optional<BigDecimal> getIncrementoCarga() { return incrementoCarga; }
}
