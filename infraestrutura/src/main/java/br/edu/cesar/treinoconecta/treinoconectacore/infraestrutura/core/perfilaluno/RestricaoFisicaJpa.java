package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.perfilaluno;

import java.time.LocalDate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.RegiaoCorporal;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class RestricaoFisicaJpa {
    @NotNull @Enumerated(EnumType.STRING) @Column(name="RFI_REGIAO") private RegiaoCorporal regiao;
    @NotNull @Column(name="RFI_DESCRICAO") private String descricao;
    @NotNull @Column(name="RFI_REGISTRO") private LocalDate registro;

    protected RestricaoFisicaJpa() { }

    public RestricaoFisicaJpa(RegiaoCorporal regiao, String descricao, LocalDate registro) {
        this.regiao = regiao;
        this.descricao = descricao;
        this.registro = registro;
    }

    public RegiaoCorporal getRegiao() { return regiao; }
    public String getDescricao() { return descricao; }
    public LocalDate getRegistro() { return registro; }
}
