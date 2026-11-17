package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino;

import java.time.Instant;
import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;
public record ConviteGrupo(Optional<Long> identificador,AlunoIdentificador aluno,SituacaoConviteGrupo situacao,Instant envio,Optional<Instant> resposta) {
    public ConviteGrupo {Objects.requireNonNull(identificador);Objects.requireNonNull(aluno);Objects.requireNonNull(situacao);Objects.requireNonNull(envio);Objects.requireNonNull(resposta);}
    public ConviteGrupo responder(AlunoIdentificador autor,boolean aceitar,Instant agora){
        if(!aluno.equals(autor))throw new NaoAutorizadoExcecao("Somente o destinatário pode responder ao convite");
        if(situacao!=SituacaoConviteGrupo.PENDENTE)throw new IllegalStateException("Convite já respondido ou cancelado");
        return new ConviteGrupo(identificador,aluno,aceitar?SituacaoConviteGrupo.ACEITO:SituacaoConviteGrupo.RECUSADO,envio,Optional.of(agora));
    }
    public ConviteGrupo cancelar(Instant agora){return situacao==SituacaoConviteGrupo.PENDENTE?new ConviteGrupo(identificador,aluno,SituacaoConviteGrupo.CANCELADO,envio,Optional.of(agora)):this;}
}
