package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica;

import java.math.BigDecimal;
import java.util.Objects;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;

public enum IndicadorFisico {
    PESO(SentidoMelhora.INDIFERENTE, false),
    IMC(SentidoMelhora.INDIFERENTE, false),
    CINTURA(SentidoMelhora.MENOR, true),
    ABDOMEN(SentidoMelhora.MENOR, true),
    QUADRIL(SentidoMelhora.MENOR, true),
    PEITORAL(SentidoMelhora.MAIOR, true),
    BRACO(SentidoMelhora.MAIOR, true),
    COXA(SentidoMelhora.MAIOR, true),
    PANTURRILHA(SentidoMelhora.MAIOR, true);

    private final SentidoMelhora sentidoMelhora;
    private final boolean medidaCorporal;

    IndicadorFisico(SentidoMelhora sentidoMelhora, boolean medidaCorporal) {
        this.sentidoMelhora = sentidoMelhora;
        this.medidaCorporal = medidaCorporal;
    }

    public boolean isMedidaCorporal() {
        return medidaCorporal;
    }

    public InterpretacaoEvolucao interpretar(BigDecimal variacaoPercentual) {
        Objects.requireNonNull(variacaoPercentual);
        if (variacaoPercentual.abs().compareTo(ParametrosAcademia.MARGEM_ESTABILIDADE_PERCENTUAL) <= 0) {
            return InterpretacaoEvolucao.ESTAVEL;
        }
        if (sentidoMelhora == SentidoMelhora.INDIFERENTE) {
            return InterpretacaoEvolucao.DEPENDE_DO_OBJETIVO;
        }
        boolean diminuiu = variacaoPercentual.signum() < 0;
        boolean melhorou = sentidoMelhora == SentidoMelhora.MENOR ? diminuiu : !diminuiu;
        return melhorou ? InterpretacaoEvolucao.MELHORA : InterpretacaoEvolucao.PIORA;
    }
}
