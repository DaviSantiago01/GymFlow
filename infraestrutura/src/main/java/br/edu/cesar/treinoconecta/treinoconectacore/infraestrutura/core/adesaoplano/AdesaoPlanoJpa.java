package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.adesaoplano;

import java.time.LocalDate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.SituacaoAdesao;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.plano.PlanoJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

@Entity @Table(name="ADP_ADESAO_PLANO")
public class AdesaoPlanoJpa implements IAgregadoJpa<AdesaoPlano> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="ADP_ID")
    private @Nullable Long identificador;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ALU_ID") private AlunoJpa aluno;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="PLA_ID") private PlanoJpa plano;
    @Column(name="ADP_PERIODICIDADE_MESES") private int periodicidadeMeses;
    @NotNull @Column(name="ADP_INICIO") private LocalDate inicio;
    @Column(name="ADP_ENCERRAMENTO") private @Nullable LocalDate encerramento;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="ADP_SITUACAO") private SituacaoAdesao situacao;
    @Column(name="ADP_QUANTIDADE_PERIODOS") private @Nullable Integer quantidadePeriodos;

    protected AdesaoPlanoJpa() { }

    public AdesaoPlanoJpa(@Nullable Long identificador, AlunoJpa aluno, PlanoJpa plano, int periodicidadeMeses,
            LocalDate inicio, @Nullable LocalDate encerramento, SituacaoAdesao situacao,
            @Nullable Integer quantidadePeriodos) {
        this.identificador = identificador;
        this.aluno = aluno;
        this.plano = plano;
        this.periodicidadeMeses = periodicidadeMeses;
        this.inicio = inicio;
        this.encerramento = encerramento;
        this.situacao = situacao;
        this.quantidadePeriodos = quantidadePeriodos;
    }

    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public AlunoJpa getAluno() { return aluno; }
    public PlanoJpa getPlano() { return plano; }
    public int getPeriodicidadeMeses() { return periodicidadeMeses; }
    public LocalDate getInicio() { return inicio; }
    public @Nullable LocalDate getEncerramento() { return encerramento; }
    public SituacaoAdesao getSituacao() { return situacao; }
    public @Nullable Integer getQuantidadePeriodos() { return quantidadePeriodos; }
}
