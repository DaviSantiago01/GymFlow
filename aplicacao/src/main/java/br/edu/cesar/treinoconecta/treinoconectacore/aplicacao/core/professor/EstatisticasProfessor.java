package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.professor;

import java.math.*;
import java.util.*;
public record EstatisticasProfessor(long quantidade,Optional<BigDecimal> auxilio,Optional<BigDecimal> pontualidade,
        Optional<BigDecimal> postura,Optional<BigDecimal> didatica,Optional<BigDecimal> adequacao,Optional<BigDecimal> geral) {
    public static EstatisticasProfessor calcular(long quantidade,long auxilio,long pontualidade,long postura,long didatica,long adequacao) {
        // Média geral calculada diretamente das somas, sem arredondar médias intermediárias.
        var soma=BigDecimal.valueOf(auxilio).add(BigDecimal.valueOf(pontualidade)).add(BigDecimal.valueOf(postura)).add(BigDecimal.valueOf(didatica)).add(BigDecimal.valueOf(adequacao));
        return new EstatisticasProfessor(quantidade,media(BigDecimal.valueOf(auxilio),quantidade),media(BigDecimal.valueOf(pontualidade),quantidade),
            media(BigDecimal.valueOf(postura),quantidade),media(BigDecimal.valueOf(didatica),quantidade),media(BigDecimal.valueOf(adequacao),quantidade),
            quantidade==0?Optional.empty():Optional.of(soma.divide(BigDecimal.valueOf(quantidade).multiply(BigDecimal.valueOf(5)),MathContext.DECIMAL128)));
    }
    private static Optional<BigDecimal> media(BigDecimal soma,long quantidade) {
        return quantidade==0?Optional.empty():Optional.of(soma.divide(BigDecimal.valueOf(quantidade),MathContext.DECIMAL128));
    }
}
