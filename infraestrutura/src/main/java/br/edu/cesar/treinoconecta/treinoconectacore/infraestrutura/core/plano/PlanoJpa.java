package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.plano;

import java.math.BigDecimal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.Plano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.SituacaoPlano;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

@Entity @Table(name="PLA_PLANO")
public class PlanoJpa implements IAgregadoJpa<Plano> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="PLA_ID")
    private @Nullable Long identificador;
    @NotNull @Size(max=Plano.TAMANHO_NOME) @Column(name="PLA_NOME") private String nome;
    @NotNull @Column(name="PLA_VALOR") private BigDecimal valor;
    @Column(name="PLA_DURACAO_MESES") private int duracaoMeses;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="PLA_SITUACAO") private SituacaoPlano situacao;

    protected PlanoJpa() { }

    public PlanoJpa(@Nullable Long identificador, String nome, BigDecimal valor, int duracaoMeses, SituacaoPlano situacao) {
        this.identificador = identificador;
        this.nome = nome;
        this.valor = valor;
        this.duracaoMeses = duracaoMeses;
        this.situacao = situacao;
    }

    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public String getNome() { return nome; }
    public BigDecimal getValor() { return valor; }
    public int getDuracaoMeses() { return duracaoMeses; }
    public SituacaoPlano getSituacao() { return situacao; }
}
