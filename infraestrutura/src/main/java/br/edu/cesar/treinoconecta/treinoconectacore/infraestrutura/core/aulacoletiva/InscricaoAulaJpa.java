package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aulacoletiva;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class InscricaoAulaJpa {
    @NotNull @Column(name="ALU_ID") private Long aluno;
    @NotNull @Column(name="IAC_DATA") private Instant data;

    protected InscricaoAulaJpa() { }

    public InscricaoAulaJpa(Long aluno, Instant data) {
        this.aluno = aluno;
        this.data = data;
    }

    public Long getAluno() { return aluno; }
    public Instant getData() { return data; }
}
