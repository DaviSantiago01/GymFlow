package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.perfilaluno;

import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.NivelExperiencia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.ObjetivoAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.PerfilAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aulacoletiva.HorarioLocalConversor;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity @Table(name="PFA_PERFIL_ALUNO")
public class PerfilAlunoJpa implements IAgregadoJpa<PerfilAluno> {
    @Id @Column(name="ALU_ID") private Long aluno;
    @Enumerated(EnumType.STRING) @Column(name="PFA_OBJETIVO") private @Nullable ObjetivoAluno objetivo;
    @Enumerated(EnumType.STRING) @Column(name="PFA_NIVEL") private @Nullable NivelExperiencia nivel;
    @Column(name="PFA_DIAS") private @Nullable String dias;
    @Convert(converter=HorarioLocalConversor.class) @Column(name="PFA_HORA_INICIO") private @Nullable LocalTime horaInicio;
    @Convert(converter=HorarioLocalConversor.class) @Column(name="PFA_HORA_FIM") private @Nullable LocalTime horaFim;
    @NotNull @Column(name="PFA_CATEGORIAS_DESATIVADAS") private String categoriasDesativadas;
    @NotNull @Column(name="PFA_ATUALIZACAO") private Instant atualizacao;
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="RFI_RESTRICAO_FISICA", joinColumns=@JoinColumn(name="ALU_ID"))
    @OrderColumn(name="RFI_INDICE")
    private List<RestricaoFisicaJpa> restricoes = new ArrayList<>();

    protected PerfilAlunoJpa() { }

    public PerfilAlunoJpa(Long aluno, @Nullable ObjetivoAluno objetivo, @Nullable NivelExperiencia nivel,
            @Nullable String dias, @Nullable LocalTime horaInicio, @Nullable LocalTime horaFim,
            String categoriasDesativadas, Instant atualizacao, List<RestricaoFisicaJpa> restricoes) {
        this.aluno = aluno;
        this.objetivo = objetivo;
        this.nivel = nivel;
        this.dias = dias;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.categoriasDesativadas = categoriasDesativadas;
        this.atualizacao = atualizacao;
        this.restricoes = restricoes;
    }

    public Long getAluno() { return aluno; }
    public @Nullable ObjetivoAluno getObjetivo() { return objetivo; }
    public @Nullable NivelExperiencia getNivel() { return nivel; }
    public @Nullable String getDias() { return dias; }
    public @Nullable LocalTime getHoraInicio() { return horaInicio; }
    public @Nullable LocalTime getHoraFim() { return horaFim; }
    public String getCategoriasDesativadas() { return categoriasDesativadas; }
    public Instant getAtualizacao() { return atualizacao; }
    public List<RestricaoFisicaJpa> getRestricoes() { return restricoes; }
}
