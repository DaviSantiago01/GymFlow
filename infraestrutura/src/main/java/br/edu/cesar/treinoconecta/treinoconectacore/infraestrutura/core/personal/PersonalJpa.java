package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.Personal;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.jspecify.annotations.Nullable;

@Entity @Table(name="PER_PERSONAL")
public class PersonalJpa implements IAgregadoJpa<Personal> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="PER_ID")
    private @Nullable Long identificador;
    @NotNull @Size(max=Personal.TAMANHO_USUARIO) @Column(name="PER_USUARIO",unique=true) private String usuario;
    @NotNull @Size(max=Personal.TAMANHO_NOME) @Column(name="PER_NOME") private String nome;

    protected PersonalJpa() { }

    public PersonalJpa(@Nullable Long identificador, String usuario, String nome) {
        this.identificador = identificador;
        this.usuario = usuario;
        this.nome = nome;
    }

    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public String getUsuario() { return usuario; }
    public String getNome() { return nome; }
}
