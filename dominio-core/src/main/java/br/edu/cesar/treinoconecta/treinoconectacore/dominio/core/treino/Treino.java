package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino;

import java.time.Instant;
import java.util.*;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.ProfessorIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;
public record Treino(Optional<TreinoIdentificador> identificador,AlunoIdentificador aluno,ProfessorIdentificador professor,
        String descricao,Instant inicio,Instant termino,boolean concluido,boolean demonstracao) implements IAgregado<TreinoIdentificador> {
    public Treino {
        Objects.requireNonNull(identificador);Objects.requireNonNull(aluno);Objects.requireNonNull(professor);
        Objects.requireNonNull(descricao);Objects.requireNonNull(inicio);Objects.requireNonNull(termino);
        Validate.isTrue(!descricao.isBlank() && descricao.length()<=120,"Informe a descrição do treino com até 120 caracteres");
        Validate.isTrue(termino.isAfter(inicio),"O término deve ser posterior ao início do treino");
    }
    public Optional<TreinoIdentificador> getIdentificador(){return identificador;}
    public void exigirAluno(AlunoIdentificador autor){if(!aluno.equals(Objects.requireNonNull(autor))) throw new NaoAutorizadoExcecao("Este treino não pertence ao aluno autenticado");}
    public void exigirAvaliavel(AlunoIdentificador autor,Instant agora){exigirAluno(autor);Validate.validState(concluido && !termino.isAfter(Objects.requireNonNull(agora)),"Somente treinos concluídos podem ser avaliados");}
}
