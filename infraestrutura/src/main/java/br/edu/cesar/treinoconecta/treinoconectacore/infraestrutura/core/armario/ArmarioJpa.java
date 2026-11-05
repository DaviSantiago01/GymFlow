package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.armario;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.Armario;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
@Entity @Table(name="ARM_ARMARIO")
public class ArmarioJpa implements IAgregadoJpa<Armario> {
    @Id @NotNull @Size(max=5) @Column(name="ARM_CODIGO") private String identificador;
    @NotNull @Size(max=150) @Column(name="ARM_LOCALIZACAO") private String localizacao;
    @NotNull @Size(max=250) @Column(name="ARM_DESCRICAO") private String descricao;
    protected ArmarioJpa() { }
    public ArmarioJpa(String identificador, String localizacao, String descricao) {
        this.identificador=identificador; this.localizacao=localizacao; this.descricao=descricao;
    }
    public String getIdentificador() { return identificador; }
    public String getLocalizacao() { return localizacao; }
    public String getDescricao() { return descricao; }
}
