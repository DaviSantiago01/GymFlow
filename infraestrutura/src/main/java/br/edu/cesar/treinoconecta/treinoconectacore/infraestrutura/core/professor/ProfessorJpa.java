package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.professor;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.Professor;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
@Entity @Table(name="PRO_PROFESSOR")
public class ProfessorJpa implements IAgregadoJpa<Professor> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="PRO_ID") private @Nullable Long identificador;
    @NotNull @Size(max=100) @Column(name="PRO_NOME") private String nome;
    @NotNull @Size(max=120) @Column(name="PRO_ESPECIALIDADE") private String especialidade;
    @NotNull @Size(max=1000) @Column(name="PRO_BIOGRAFIA") private String biografia;
    @Column(name="PRO_DEMONSTRACAO") private boolean demonstracao;
    protected ProfessorJpa() { }
    public ProfessorJpa(@Nullable Long id,String nome,String especialidade,String biografia,boolean demonstracao) {
        this.identificador=id;this.nome=nome;this.especialidade=especialidade;this.biografia=biografia;this.demonstracao=demonstracao;
    }
    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public String getNome() { return nome; } public String getEspecialidade() { return especialidade; }
    public String getBiografia() { return biografia; } public boolean isDemonstracao() { return demonstracao; }
}
