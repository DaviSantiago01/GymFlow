package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.problemaequipamento;

import java.time.LocalDate;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.equipamento.EquipamentoJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "PEQ_PROBLEMA_EQUIPAMENTO")
public class ProblemaEquipamentoJpa implements IAgregadoJpa<ProblemaEquipamento> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PEQ_ID")
    private @Nullable Long identificador;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EQP_ID")
    private EquipamentoJpa equipamento;

    @NotNull
    @Size(max = 100)
    @Column(name = "PEQ_TITULO")
    private String titulo;

    @NotNull
    @Size(max = 800)
    @Column(name = "PEQ_RELATO")
    private String relato;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "PEQ_GRAVIDADE")
    private GravidadeProblema gravidade;

    @Column(name = "PEQ_RISCO")
    private boolean risco;

    @Size(max = 180)
    @Column(name = "PEQ_EVIDENCIA")
    private @Nullable String evidencia;

    @NotNull
    @Column(name = "PEQ_DATA_RELATO")
    private LocalDate dataRelato;

    @NotNull
    @Size(max = 100)
    @Column(name = "PEQ_RESPONSAVEL")
    private String responsavel;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "PEQ_SITUACAO")
    private SituacaoProblema situacao;

    @Column(name = "PEQ_DATA_ENCERRAMENTO")
    private @Nullable LocalDate dataEncerramento;

    @Size(max = 800)
    @Column(name = "PEQ_DESFECHO")
    private @Nullable String desfecho;

    protected ProblemaEquipamentoJpa() { }

    public ProblemaEquipamentoJpa(@Nullable Long identificador,
            EquipamentoJpa equipamento,
            String titulo,
            String relato,
            GravidadeProblema gravidade,
            boolean risco,
            @Nullable String evidencia,
            LocalDate dataRelato,
            String responsavel,
            SituacaoProblema situacao,
            @Nullable LocalDate dataEncerramento,
            @Nullable String desfecho) {
        this.identificador = identificador;
        this.equipamento = equipamento;
        this.titulo = titulo;
        this.relato = relato;
        this.gravidade = gravidade;
        this.risco = risco;
        this.evidencia = evidencia;
        this.dataRelato = dataRelato;
        this.responsavel = responsavel;
        this.situacao = situacao;
        this.dataEncerramento = dataEncerramento;
        this.desfecho = desfecho;
    }

    public Long getIdentificador() {
        return Objects.requireNonNull(identificador);
    }

    public EquipamentoJpa getEquipamento() {
        return equipamento;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getRelato() {
        return relato;
    }

    public GravidadeProblema getGravidade() {
        return gravidade;
    }

    public boolean getRisco() {
        return risco;
    }

    public @Nullable String getEvidencia() {
        return evidencia;
    }

    public LocalDate getDataRelato() {
        return dataRelato;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public SituacaoProblema getSituacao() {
        return situacao;
    }

    public @Nullable LocalDate getDataEncerramento() {
        return dataEncerramento;
    }

    public @Nullable String getDesfecho() {
        return desfecho;
    }
}
