package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.treino;

import java.time.Instant;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.Treino;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;
public record TreinoProjecao(String identificador,String professor,String professorNome,String descricao,Instant inicio,Instant termino,boolean demonstracao,Optional<String> avaliacao,boolean jaAvaliou) implements IProjecao<Treino> {
    public TreinoProjecao(Long id,Long professor,String professorNome,String descricao,Instant inicio,Instant termino,boolean demonstracao,@Nullable Long avaliacao,boolean jaAvaliou){this(id.toString(),professor.toString(),professorNome,descricao,inicio,termino,demonstracao,Optional.ofNullable(avaliacao).map(Object::toString),jaAvaliou);}
}
