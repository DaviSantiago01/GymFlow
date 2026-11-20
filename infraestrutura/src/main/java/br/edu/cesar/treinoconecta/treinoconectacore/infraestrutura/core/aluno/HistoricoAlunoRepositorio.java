package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import java.time.Instant;
import java.util.Objects;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.TipoAlteracaoAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IHistoricoAlunoRepositorio;
import jakarta.persistence.EntityManager;

public final class HistoricoAlunoRepositorio implements IHistoricoAlunoRepositorio {
    private static final int TAMANHO_DESCRICAO = 250;

    private final IHistoricoAlunoRepositorioJpa jpa;
    private final EntityManager em;

    public HistoricoAlunoRepositorio(IHistoricoAlunoRepositorioJpa jpa, EntityManager em) {
        this.jpa = Objects.requireNonNull(jpa);
        this.em = Objects.requireNonNull(em);
    }

    @Override
    public void registrar(AlunoIdentificador aluno, TipoAlteracaoAluno tipo, String descricao, String responsavel,
            Instant instante) {
        Objects.requireNonNull(aluno);
        var texto = descricao.length() > TAMANHO_DESCRICAO ? descricao.substring(0, TAMANHO_DESCRICAO) : descricao;
        jpa.save(new HistoricoAlunoJpa(em.getReference(AlunoJpa.class, Long.valueOf(aluno.toString())), instante,
                Objects.requireNonNull(tipo), texto, Objects.requireNonNull(responsavel)));
    }
}
