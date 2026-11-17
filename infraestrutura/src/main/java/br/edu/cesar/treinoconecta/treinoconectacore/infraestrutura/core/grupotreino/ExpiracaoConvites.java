package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.grupotreino;

import java.time.Clock;
import java.util.Objects;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.spi.IGrupoTreinoRepositorio;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;
public final class ExpiracaoConvites {
    private final IGrupoTreinoRepositorio grupos;private final Clock relogio;private final TransactionTemplate transacao;
    public ExpiracaoConvites(IGrupoTreinoRepositorio grupos,Clock relogio,TransactionTemplate transacao){this.grupos=Objects.requireNonNull(grupos);this.relogio=Objects.requireNonNull(relogio);this.transacao=Objects.requireNonNull(transacao);}
    @Scheduled(fixedDelayString="${treino.grupos.intervalo-ms:1000}")
    public void processar(){for(var id:grupos.vencidos(relogio.instant(),100))transacao.executeWithoutResult(s->{var g=grupos.bloquear(id);g.expirar(relogio.instant());grupos.alterar(g);});}
}
