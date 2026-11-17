package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.profissional;

import java.util.Objects;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Especialidade;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/** A chave normalizada é a chave primária junto com o profissional: o banco também impede especialidades repetidas. */
@Embeddable
public class EspecialidadeJpa {
    @NotNull @Size(max=Especialidade.TAMANHO_NOME) @Column(name="PES_CHAVE") private String chave;
    @NotNull @Size(max=Especialidade.TAMANHO_NOME) @Column(name="PES_NOME") private String nome;

    protected EspecialidadeJpa() { }

    public EspecialidadeJpa(String chave, String nome) {
        this.chave = chave;
        this.nome = nome;
    }

    public String getChave() { return chave; }
    public String getNome() { return nome; }

    @Override
    public boolean equals(@Nullable Object objeto) {
        return objeto instanceof EspecialidadeJpa outra && Objects.equals(chave, outra.chave);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(chave);
    }
}
