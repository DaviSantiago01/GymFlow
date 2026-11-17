package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.mensalidade;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.Mensalidade;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.SituacaoMensalidade;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.adesaoplano.AdesaoPlanoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

@Entity @Table(name="MEN_MENSALIDADE")
public class MensalidadeJpa implements IAgregadoJpa<Mensalidade> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="MEN_ID")
    private @Nullable Long identificador;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ADP_ID") private AdesaoPlanoJpa adesao;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ALU_ID") private AlunoJpa aluno;
    @NotNull @Column(name="MEN_PERIODO_INICIO") private LocalDate periodoInicio;
    @NotNull @Column(name="MEN_PERIODO_FIM") private LocalDate periodoFim;
    @NotNull @Column(name="MEN_VENCIMENTO") private LocalDate vencimento;
    @NotNull @Column(name="MEN_VALOR") private BigDecimal valor;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="MEN_SITUACAO") private SituacaoMensalidade situacao;
    @OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL, orphanRemoval=true)
    @JoinColumn(name="MEN_ID", nullable=false)
    @OrderBy("registro")
    private List<PagamentoJpa> pagamentos = new ArrayList<>();

    protected MensalidadeJpa() { }

    public MensalidadeJpa(@Nullable Long identificador, AdesaoPlanoJpa adesao, AlunoJpa aluno, LocalDate periodoInicio,
            LocalDate periodoFim, LocalDate vencimento, BigDecimal valor, SituacaoMensalidade situacao,
            List<PagamentoJpa> pagamentos) {
        this.identificador = identificador;
        this.adesao = adesao;
        this.aluno = aluno;
        this.periodoInicio = periodoInicio;
        this.periodoFim = periodoFim;
        this.vencimento = vencimento;
        this.valor = valor;
        this.situacao = situacao;
        this.pagamentos = pagamentos;
    }

    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public AdesaoPlanoJpa getAdesao() { return adesao; }
    public AlunoJpa getAluno() { return aluno; }
    public LocalDate getPeriodoInicio() { return periodoInicio; }
    public LocalDate getPeriodoFim() { return periodoFim; }
    public LocalDate getVencimento() { return vencimento; }
    public BigDecimal getValor() { return valor; }
    public SituacaoMensalidade getSituacao() { return situacao; }
    public List<PagamentoJpa> getPagamentos() { return pagamentos; }
}
