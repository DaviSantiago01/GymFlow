package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor;

import java.time.Instant;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.TreinoIdentificador;
import java.util.*;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.ProfessorIdentificador;
public record AvaliacaoProfessor(Optional<AvaliacaoProfessorIdentificador> identificador, AlunoIdentificador aluno,
        ProfessorIdentificador professor, Optional<TreinoIdentificador> treino, NotasAvaliacao notas, Optional<String> comentario, Instant criacao,
        Instant atualizacao) implements IAgregado<AvaliacaoProfessorIdentificador> {
    public AvaliacaoProfessor {
        Objects.requireNonNull(identificador);Objects.requireNonNull(aluno);Objects.requireNonNull(professor);
        Objects.requireNonNull(treino);Objects.requireNonNull(notas);Objects.requireNonNull(comentario);Objects.requireNonNull(criacao);Objects.requireNonNull(atualizacao);
        comentario.ifPresent(c -> Validate.isTrue(c.length()<=500,"O comentário deve ter até 500 caracteres"));
        comentario=comentario.map(String::strip).filter(c -> !c.isEmpty());
        Validate.isTrue(!atualizacao.isBefore(criacao),"Atualização anterior à criação");
    }
    public static AvaliacaoProfessor criar(AlunoIdentificador aluno, ProfessorIdentificador professor, TreinoIdentificador treino, NotasAvaliacao notas,Optional<String> comentario,Instant agora) {
        return new AvaliacaoProfessor(Optional.empty(),aluno,professor,Optional.of(treino),notas,comentario,agora,agora);
    }
    public AvaliacaoProfessor editar(AlunoIdentificador autor, NotasAvaliacao novas,Optional<String> texto,Instant agora) {
        exigirAutor(autor);
        Validate.validState(!Objects.requireNonNull(agora).isBefore(atualizacao),"Relógio anterior à última atualização");
        return new AvaliacaoProfessor(identificador,aluno,professor,treino,novas,texto,criacao,agora);
    }
    public void exigirAutor(AlunoIdentificador autor) {
        if(!aluno.equals(Objects.requireNonNull(autor))) throw new NaoAutorizadoExcecao("Somente o autor pode acessar ou modificar esta avaliação");
    }
    public Optional<AvaliacaoProfessorIdentificador> getIdentificador() { return identificador; }
}
