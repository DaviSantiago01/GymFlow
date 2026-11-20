package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoGestaoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.HistoricoAlunoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.spi.IAlunoGestaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;

public final class AlunoGestaoRepositorio implements IAlunoGestaoRepositorio {
    private final IAlunoGestaoJpa alunos;
    private final IHistoricoAlunoRepositorioJpa historico;

    public AlunoGestaoRepositorio(IAlunoGestaoJpa alunos, IHistoricoAlunoRepositorioJpa historico) {
        this.alunos = Objects.requireNonNull(alunos);
        this.historico = Objects.requireNonNull(historico);
    }

    /** O trecho já chega em minúsculas e, se for um CPF, sem pontuação. */
    @Override
    public List<AlunoGestaoProjecao> pesquisar(Optional<String> busca, LocalDate hoje) {
        var like = busca.map(trecho -> "%" + EscapeCharacter.DEFAULT.escape(trecho) + "%").orElse(null);
        return alunos.pesquisar(like, Objects.requireNonNull(hoje));
    }

    @Override
    public Optional<AlunoGestaoProjecao> projetar(AlunoIdentificador aluno, LocalDate hoje) {
        return alunos.projetar(Long.valueOf(aluno.toString()), Objects.requireNonNull(hoje));
    }

    @Override
    public List<HistoricoAlunoProjecao> historico(AlunoIdentificador aluno) {
        return historico.doAluno(Long.valueOf(aluno.toString()));
    }
}
