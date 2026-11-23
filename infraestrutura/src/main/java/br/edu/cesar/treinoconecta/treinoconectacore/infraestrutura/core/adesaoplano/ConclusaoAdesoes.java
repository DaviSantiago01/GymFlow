package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.adesaoplano;

import java.util.Objects;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoServico;

/** Adaptador agendado: conclui as adesões cuja recorrência terminou, sem depender de uma requisição HTTP. */
public final class ConclusaoAdesoes {
    private final AdesaoPlanoServico adesoes;
    private final TransactionTemplate transacao;

    public ConclusaoAdesoes(AdesaoPlanoServico adesoes, TransactionTemplate transacao) {
        this.adesoes = Objects.requireNonNull(adesoes);
        this.transacao = Objects.requireNonNull(transacao);
    }

    @Scheduled(fixedDelayString = "${treino.adesoes.intervalo-ms:60000}")
    public void processar() {
        transacao.executeWithoutResult(estado -> adesoes.concluirPrazosCumpridos());
    }
}
