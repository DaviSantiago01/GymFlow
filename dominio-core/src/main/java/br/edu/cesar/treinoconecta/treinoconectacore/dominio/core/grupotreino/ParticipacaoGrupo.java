package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino;

import java.time.Instant;
import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
public record ParticipacaoGrupo(Optional<Long> identificador,AlunoIdentificador aluno,Instant entrada,Optional<Instant> saida) {
    public ParticipacaoGrupo {Objects.requireNonNull(identificador);Objects.requireNonNull(aluno);Objects.requireNonNull(entrada);Objects.requireNonNull(saida);}
    public boolean ativa(){return saida.isEmpty();}
    public ParticipacaoGrupo sair(Instant agora){if(!ativa())throw new IllegalStateException("Participação já encerrada");return new ParticipacaoGrupo(identificador,aluno,entrada,Optional.of(agora));}
}
