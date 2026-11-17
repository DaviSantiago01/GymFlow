package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.academia.AcademiaJpa;
import jakarta.persistence.EntityManager;

/** Cria alunos de teste na academia padrão (id 1), criada pela migração V6. */
public final class AlunoJpaTeste {
    private AlunoJpaTeste() { }

    public static AlunoJpa novo(EntityManager em, String usuario, String nome) {
        return new AlunoJpa(null, usuario, nome, em.getReference(AcademiaJpa.class, 1L));
    }
}
