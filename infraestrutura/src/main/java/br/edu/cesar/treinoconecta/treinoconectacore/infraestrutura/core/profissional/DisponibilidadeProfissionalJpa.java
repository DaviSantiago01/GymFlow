package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.profissional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

@Entity @Table(name="DSP_DISPONIBILIDADE_PROFISSIONAL")
public class DisponibilidadeProfissionalJpa {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="DSP_ID")
    private @Nullable Long identificador;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="DSP_DIA") private DayOfWeek dia;
    @NotNull @Column(name="DSP_INICIO") private LocalTime inicio;
    @NotNull @Column(name="DSP_FIM") private LocalTime fim;

    protected DisponibilidadeProfissionalJpa() { }

    public DisponibilidadeProfissionalJpa(@Nullable Long identificador, DayOfWeek dia, LocalTime inicio, LocalTime fim) {
        this.identificador = identificador;
        this.dia = dia;
        this.inicio = inicio;
        this.fim = fim;
    }

    public @Nullable Long getIdentificador() { return identificador; }
    public DayOfWeek getDia() { return dia; }
    public LocalTime getInicio() { return inicio; }
    public LocalTime getFim() { return fim; }
}
