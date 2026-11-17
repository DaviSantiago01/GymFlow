package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.vinculopersonal;

import java.time.LocalDate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.SituacaoVinculo;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.profissional.ProfissionalJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

@Entity @Table(name="VPA_VINCULO_PERSONAL_ALUNO")
public class VinculoPersonalAlunoJpa implements IAgregadoJpa<VinculoPersonalAluno> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="VPA_ID")
    private @Nullable Long identificador;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="PRF_ID") private ProfissionalJpa profissional;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ALU_ID") private AlunoJpa aluno;
    @NotNull @Column(name="VPA_INICIO") private LocalDate inicio;
    @Column(name="VPA_ENCERRAMENTO") private @Nullable LocalDate encerramento;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="VPA_SITUACAO") private SituacaoVinculo situacao;

    protected VinculoPersonalAlunoJpa() { }

    public VinculoPersonalAlunoJpa(@Nullable Long identificador, ProfissionalJpa profissional, AlunoJpa aluno,
            LocalDate inicio, @Nullable LocalDate encerramento, SituacaoVinculo situacao) {
        this.identificador = identificador;
        this.profissional = profissional;
        this.aluno = aluno;
        this.inicio = inicio;
        this.encerramento = encerramento;
        this.situacao = situacao;
    }

    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public ProfissionalJpa getProfissional() { return profissional; }
    public AlunoJpa getAluno() { return aluno; }
    public LocalDate getInicio() { return inicio; }
    public @Nullable LocalDate getEncerramento() { return encerramento; }
    public SituacaoVinculo getSituacao() { return situacao; }
}
