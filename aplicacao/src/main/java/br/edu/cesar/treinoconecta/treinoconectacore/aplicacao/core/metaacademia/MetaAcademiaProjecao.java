package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.metaacademia;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.MetaAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.SituacaoMetaAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.IndicadorAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.SentidoIndicador;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public record MetaAcademiaProjecao(String identificador, IndicadorAcademia indicador, String descricao,
        SentidoIndicador sentido, BigDecimal valorDesejado, LocalDate inicio, LocalDate fim,
        SituacaoMetaAcademia situacao, Instant criacao, String responsavel) implements IProjecao<MetaAcademia> {
    public MetaAcademiaProjecao(Long identificador, IndicadorAcademia indicador, BigDecimal valorDesejado, LocalDate inicio,
            LocalDate fim, SituacaoMetaAcademia situacao, Instant criacao, String responsavel) {
        this(identificador.toString(), indicador, indicador.getDescricao(), indicador.getSentido(), valorDesejado, inicio,
                fim, situacao, criacao, responsavel);
    }
}
