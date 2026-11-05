package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.reserva;

import java.time.Clock;
import java.util.Objects;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.spi.IReservaArmarioRepositorio;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;

/** Adaptador agendado registrado explicitamente; não depende de requisição HTTP. */
public final class ExpiracaoReservas {
    private final IReservaArmarioRepositorio reservas;
    private final Clock relogio;
    private final TransactionTemplate transacao;
    public ExpiracaoReservas(IReservaArmarioRepositorio reservas, Clock relogio, TransactionTemplate transacao) {
        this.reservas=Objects.requireNonNull(reservas); this.relogio=Objects.requireNonNull(relogio);
        this.transacao=Objects.requireNonNull(transacao);
    }
    @Scheduled(fixedDelayString="${treino.reservas.intervalo-ms:1000}")
    public void processar() { transacao.executeWithoutResult(estado -> reservas.vencerTodas(relogio.instant())); }
}
