package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.ordemmanutencao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.problemaequipamento.ProblemaEquipamentoJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "OMT_ORDEM_MANUTENCAO")
public class OrdemManutencaoJpa implements IAgregadoJpa<OrdemManutencao> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OMT_ID")
    private @Nullable Long identificador;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PEQ_ID")
    private ProblemaEquipamentoJpa problema;

    @NotNull
    @Size(max = 100)
    @Column(name = "OMT_TECNICO")
    private String tecnico;

    @NotNull
    @Size(max = 800)
    @Column(name = "OMT_DIAGNOSTICO")
    private String diagnostico;

    @NotNull
    @Column(name = "OMT_ABERTURA")
    private LocalDate abertura;

    @NotNull
    @Size(max = 100)
    @Column(name = "OMT_RESPONSAVEL")
    private String responsavel;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "OMT_SITUACAO")
    private SituacaoOrdem situacao;

    @Column(name = "OMT_INICIO")
    private @Nullable LocalDate inicio;

    @Column(name = "OMT_TERMINO")
    private @Nullable LocalDate termino;

    @Column(name = "OMT_CUSTO_ESTIMADO", precision = 12, scale = 2)
    private @Nullable BigDecimal custoEstimado;

    @Column(name = "OMT_CUSTO_REAL", precision = 12, scale = 2)
    private @Nullable BigDecimal custoReal;

    @Size(max = 800)
    @Column(name = "OMT_SERVICO_EXECUTADO")
    private @Nullable String servicoExecutado;

    @Size(max = 800)
    @Column(name = "OMT_JUSTIFICATIVA")
    private @Nullable String justificativa;

    protected OrdemManutencaoJpa() { }

    public OrdemManutencaoJpa(@Nullable Long identificador,
            ProblemaEquipamentoJpa problema,
            String tecnico,
            String diagnostico,
            LocalDate abertura,
            String responsavel,
            SituacaoOrdem situacao,
            @Nullable LocalDate inicio,
            @Nullable LocalDate termino,
            @Nullable BigDecimal custoEstimado,
            @Nullable BigDecimal custoReal,
            @Nullable String servicoExecutado,
            @Nullable String justificativa) {
        this.identificador = identificador;
        this.problema = problema;
        this.tecnico = tecnico;
        this.diagnostico = diagnostico;
        this.abertura = abertura;
        this.responsavel = responsavel;
        this.situacao = situacao;
        this.inicio = inicio;
        this.termino = termino;
        this.custoEstimado = custoEstimado;
        this.custoReal = custoReal;
        this.servicoExecutado = servicoExecutado;
        this.justificativa = justificativa;
    }

    public Long getIdentificador() {
        return Objects.requireNonNull(identificador);
    }

    public ProblemaEquipamentoJpa getProblema() {
        return problema;
    }

    public String getTecnico() {
        return tecnico;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public LocalDate getAbertura() {
        return abertura;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public SituacaoOrdem getSituacao() {
        return situacao;
    }

    public @Nullable LocalDate getInicio() {
        return inicio;
    }

    public @Nullable LocalDate getTermino() {
        return termino;
    }

    public @Nullable BigDecimal getCustoEstimado() {
        return custoEstimado;
    }

    public @Nullable BigDecimal getCustoReal() {
        return custoReal;
    }

    public @Nullable String getServicoExecutado() {
        return servicoExecutado;
    }

    public @Nullable String getJustificativa() {
        return justificativa;
    }
}
