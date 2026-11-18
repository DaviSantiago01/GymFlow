package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.equipamento;

import java.time.LocalDate;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;

import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "EQP_EQUIPAMENTO")
public class EquipamentoJpa implements IAgregadoJpa<Equipamento> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EQP_ID")
    private @Nullable Long identificador;

    @NotNull
    @Size(max = 24)
    @Column(name = "EQP_PATRIMONIO")
    private String patrimonio;

    @Size(max = 40)
    @Column(name = "EQP_SERIE")
    private @Nullable String serie;

    @NotNull
    @Size(max = 100)
    @Column(name = "EQP_NOME")
    private String nome;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "EQP_TIPO")
    private TipoEquipamento tipo;

    @NotNull
    @Size(max = 60)
    @Column(name = "EQP_MARCA")
    private String marca;

    @NotNull
    @Size(max = 60)
    @Column(name = "EQP_MODELO")
    private String modelo;

    @NotNull
    @Column(name = "EQP_AQUISICAO")
    private LocalDate aquisicao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "EQP_AREA")
    private AreaEquipamento area;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "EQP_SITUACAO")
    private SituacaoEquipamento situacao;

    @Column(name = "EQP_PERIODICIDADE")
    private int periodicidade;

    @Column(name = "EQP_ULTIMA_INSPECAO")
    private @Nullable LocalDate ultimaInspecao;

    protected EquipamentoJpa() { }

    public EquipamentoJpa(@Nullable Long identificador,
            String patrimonio,
            @Nullable String serie,
            String nome,
            TipoEquipamento tipo,
            String marca,
            String modelo,
            LocalDate aquisicao,
            AreaEquipamento area,
            SituacaoEquipamento situacao,
            int periodicidade,
            @Nullable LocalDate ultimaInspecao) {
        this.identificador = identificador;
        this.patrimonio = patrimonio;
        this.serie = serie;
        this.nome = nome;
        this.tipo = tipo;
        this.marca = marca;
        this.modelo = modelo;
        this.aquisicao = aquisicao;
        this.area = area;
        this.situacao = situacao;
        this.periodicidade = periodicidade;
        this.ultimaInspecao = ultimaInspecao;
    }

    public Long getIdentificador() {
        return Objects.requireNonNull(identificador);
    }

    public String getPatrimonio() {
        return patrimonio;
    }

    public @Nullable String getSerie() {
        return serie;
    }

    public String getNome() {
        return nome;
    }

    public TipoEquipamento getTipo() {
        return tipo;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public LocalDate getAquisicao() {
        return aquisicao;
    }

    public AreaEquipamento getArea() {
        return area;
    }

    public SituacaoEquipamento getSituacao() {
        return situacao;
    }

    public int getPeriodicidade() {
        return periodicidade;
    }

    public @Nullable LocalDate getUltimaInspecao() {
        return ultimaInspecao;
    }
}
