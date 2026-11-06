package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino;

import java.time.Instant;
import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;
public record ConviteTreino(Optional<Long> identificador,AlunoIdentificador aluno,SituacaoConviteTreino situacao,Instant criacao,Optional<Instant> resposta,Optional<Instant> cancelamento,Optional<String> motivo) {
    public ConviteTreino {Objects.requireNonNull(identificador);Objects.requireNonNull(aluno);Objects.requireNonNull(situacao);Objects.requireNonNull(criacao);Objects.requireNonNull(resposta);Objects.requireNonNull(cancelamento);Objects.requireNonNull(motivo);}
    public SituacaoConviteTreino situacaoEm(Instant prazo,Instant agora){return situacao==SituacaoConviteTreino.PENDENTE && !agora.isBefore(prazo)?SituacaoConviteTreino.EXPIRADO:situacao;}
    public ConviteTreino expirar(Instant prazo,Instant agora){return situacaoEm(prazo,agora)!=situacao?new ConviteTreino(identificador,aluno,SituacaoConviteTreino.EXPIRADO,criacao,resposta,cancelamento,motivo):this;}
    public void exigirDestinatario(AlunoIdentificador autor){if(!aluno.equals(autor))throw new NaoAutorizadoExcecao("Somente o destinatário pode alterar sua presença");}
    public ConviteTreino responder(AlunoIdentificador autor,boolean confirmar,Instant prazo,Instant agora){
        exigirDestinatario(autor);
        if(situacaoEm(prazo,agora)!=SituacaoConviteTreino.PENDENTE)throw new IllegalStateException("Convite já respondido, cancelado ou prazo encerrado");
        return new ConviteTreino(identificador,aluno,confirmar?SituacaoConviteTreino.CONFIRMADO:SituacaoConviteTreino.RECUSADO,criacao,Optional.of(agora),cancelamento,motivo);
    }
    public ConviteTreino cancelarPresenca(AlunoIdentificador autor,Instant inicio,Instant agora){
        exigirDestinatario(autor);
        if(situacao!=SituacaoConviteTreino.CONFIRMADO)throw new IllegalStateException("Não existe presença confirmada para cancelar");
        if(agora.isAfter(inicio.minusSeconds(3600)))throw new IllegalStateException("A presença só pode ser cancelada até uma hora antes do treino");
        return cancelar(agora,"DESISTENCIA");
    }
    public ConviteTreino cancelar(Instant agora,String causa){return situacao==SituacaoConviteTreino.PENDENTE || situacao==SituacaoConviteTreino.CONFIRMADO?new ConviteTreino(identificador,aluno,SituacaoConviteTreino.CANCELADO,criacao,resposta,Optional.of(agora),Optional.of(causa)):this;}
}
