package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor;

import java.time.Instant;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor.*;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;
public record AvaliacaoProfessorProjecao(String identificador,String professor,String professorNome,String alunoNome,
        NotasAvaliacao notas,Optional<String> comentario,Instant criacao,Instant atualizacao,boolean propria,Optional<String> treino,Optional<Instant> treinoInicio,boolean demonstracao) implements IProjecao<AvaliacaoProfessor> {
    public AvaliacaoProfessorProjecao(Long id,Long professor,String professorNome,String alunoNome,int auxilio,int pontualidade,
            int postura,int didatica,int adequacao,@Nullable String comentario,Instant criacao,Instant atualizacao,boolean propria,@Nullable Long treino,@Nullable Instant treinoInicio,@Nullable Boolean demonstracao) {
        this(id.toString(),professor.toString(),professorNome,alunoNome,new NotasAvaliacao(auxilio,pontualidade,postura,didatica,adequacao),Optional.ofNullable(comentario),criacao,atualizacao,propria,Optional.ofNullable(treino).map(Object::toString),Optional.ofNullable(treinoInicio),Boolean.TRUE.equals(demonstracao));
    }
}
