package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Cobranças de uma adesão. Com recorrência, lista todas as parcelas contratadas, inclusive as previstas; sem ela, lista
 * as mensalidades já geradas. "Restantes" conta as parcelas que ainda não foram pagas.
 */
public record CronogramaAdesao(Optional<Integer> totalParcelas, Optional<LocalDate> terminoPrevisto,
        List<ParcelaPlano> parcelas, long pagas, long abertas, long atrasadas, long previstas, long restantes,
        BigDecimal valorRestante) {
    public static CronogramaAdesao de(Optional<Integer> totalParcelas, Optional<LocalDate> terminoPrevisto,
            List<ParcelaPlano> parcelas) {
        var restantes = parcelas.stream().filter(parcela -> parcela.situacao() != SituacaoParcela.PAGA).toList();
        return new CronogramaAdesao(totalParcelas, terminoPrevisto, List.copyOf(parcelas), contar(parcelas, SituacaoParcela.PAGA),
                contar(parcelas, SituacaoParcela.ABERTA), contar(parcelas, SituacaoParcela.ATRASADA),
                contar(parcelas, SituacaoParcela.PREVISTA), restantes.size(),
                restantes.stream().map(ParcelaPlano::valor).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static long contar(List<ParcelaPlano> parcelas, SituacaoParcela situacao) {
        return parcelas.stream().filter(parcela -> parcela.situacao() == situacao).count();
    }
}
