package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.recomendacao;

import java.time.Clock;
import java.util.Objects;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.spi.IRecomendacaoRepositorio;

/** RN015: recomendações sem resposta passam a expiradas depois da validade, mesmo que ninguém abra a lista. */
public final class ExpiracaoRecomendacoes {
    private static final int LOTE = 100;

    private final IRecomendacaoRepositorio recomendacoes;
    private final Clock relogio;
    private final TransactionTemplate transacao;

    public ExpiracaoRecomendacoes(IRecomendacaoRepositorio recomendacoes, Clock relogio, TransactionTemplate transacao) {
        this.recomendacoes = Objects.requireNonNull(recomendacoes);
        this.relogio = Objects.requireNonNull(relogio);
        this.transacao = Objects.requireNonNull(transacao);
    }

    @Scheduled(fixedDelayString = "${treino.recomendacoes.intervalo-ms:600000}", initialDelay = 30000)
    public void processar() {
        var hoje = relogio.instant().atZone(ParametrosAcademia.FUSO).toLocalDate();
        for (var identificador : recomendacoes.buscarVencidas(hoje, LOTE)) {
            transacao.executeWithoutResult(estado -> {
                var recomendacao = recomendacoes.bloquear(identificador);
                if (recomendacao.vencidaEm(hoje)) {
                    recomendacoes.alterar(recomendacao.expirar(hoje));
                }
            });
        }
    }
}
