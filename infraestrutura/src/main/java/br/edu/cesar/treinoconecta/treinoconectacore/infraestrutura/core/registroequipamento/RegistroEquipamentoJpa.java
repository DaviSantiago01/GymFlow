package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.registroequipamento;

import java.time.LocalDate;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.equipamento.EquipamentoJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "REQ_REGISTRO_EQUIPAMENTO")
public class RegistroEquipamentoJpa implements IAgregadoJpa<RegistroEquipamento> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REQ_ID")
    private @Nullable Long identificador;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EQP_ID")
    private EquipamentoJpa equipamento;

    @NotNull
    @Column(name = "REQ_DATA")
    private LocalDate data;

    @NotNull
    @Size(max = 100)
    @Column(name = "REQ_RESPONSAVEL")
    private String responsavel;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "REQ_AREA_ANTERIOR")
    private AreaEquipamento areaAnterior;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "REQ_AREA_NOVA")
    private AreaEquipamento areaNova;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "REQ_SITUACAO_ANTERIOR")
    private SituacaoEquipamento situacaoAnterior;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "REQ_SITUACAO_NOVA")
    private SituacaoEquipamento situacaoNova;

    @Enumerated(EnumType.STRING)
    @Column(name = "REQ_RESULTADO")
    private @Nullable ResultadoInspecao resultado;

    @Size(max = 800)
    @Column(name = "REQ_OBSERVACAO")
    private @Nullable String observacao;

    protected RegistroEquipamentoJpa() { }

    public RegistroEquipamentoJpa(@Nullable Long identificador,
            EquipamentoJpa equipamento,
            LocalDate data,
            String responsavel,
            AreaEquipamento areaAnterior,
            AreaEquipamento areaNova,
            SituacaoEquipamento situacaoAnterior,
            SituacaoEquipamento situacaoNova,
            @Nullable ResultadoInspecao resultado,
            @Nullable String observacao) {
        this.identificador = identificador;
        this.equipamento = equipamento;
        this.data = data;
        this.responsavel = responsavel;
        this.areaAnterior = areaAnterior;
        this.areaNova = areaNova;
        this.situacaoAnterior = situacaoAnterior;
        this.situacaoNova = situacaoNova;
        this.resultado = resultado;
        this.observacao = observacao;
    }

    public Long getIdentificador() {
        return Objects.requireNonNull(identificador);
    }

    public EquipamentoJpa getEquipamento() {
        return equipamento;
    }

    public LocalDate getData() {
        return data;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public AreaEquipamento getAreaAnterior() {
        return areaAnterior;
    }

    public AreaEquipamento getAreaNova() {
        return areaNova;
    }

    public SituacaoEquipamento getSituacaoAnterior() {
        return situacaoAnterior;
    }

    public SituacaoEquipamento getSituacaoNova() {
        return situacaoNova;
    }

    public @Nullable ResultadoInspecao getResultado() {
        return resultado;
    }

    public @Nullable String getObservacao() {
        return observacao;
    }
}
