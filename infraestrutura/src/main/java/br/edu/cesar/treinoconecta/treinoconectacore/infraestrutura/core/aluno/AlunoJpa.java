package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.Aluno;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.jspecify.annotations.Nullable;
@Entity @Table(name="ALU_ALUNO")
public class AlunoJpa implements IAgregadoJpa<Aluno> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="ALU_ID")
    private @Nullable Long identificador;
    @NotNull @Size(max=100) @Column(name="ALU_USUARIO",unique=true) private String usuario;
    @NotNull @Size(max=100) @Column(name="ALU_NOME") private String nome;
    protected AlunoJpa() { }
    public AlunoJpa(@Nullable Long identificador, String usuario, String nome) {
        this.identificador=identificador; this.usuario=usuario; this.nome=nome;
    }
    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public String getUsuario() { return usuario; }
    public String getNome() { return nome; }
}
