package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.spi;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.DadosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.PeriodoAnalise;

/** Consulta os registros da operação da academia usados no cálculo dos indicadores. */
public interface IFonteDadosRelatorio {
    /**
     * Carrega os registros necessários para analisar datas entre {@code desde} e {@code ate}. Matrículas e trancamentos
     * vêm completos, porque a classificação de uma matrícula depende das anteriores do mesmo aluno.
     */
    DadosAcademia carregar(LocalDate desde, LocalDate ate);

    Set<String> modalidades();

    /** Nome de quem gera o relatório, guardado no histórico (RN017). */
    String nomeDoUsuario(String usuario);

    /** RN018: descreve os registros do período incluídos ou corrigidos depois do instante informado. */
    List<String> alteracoesApos(PeriodoAnalise periodo, Instant geracao);
}
