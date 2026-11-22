package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio.spi;

import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio.OpcaoFiltro;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio.RelatorioConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio.RelatorioProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.FiltrosRelatorio;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IRelatorioAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<RelatorioProjecao, RelatorioConsulta> {
    /** Descrição legível dos filtros, com os nomes do plano, do personal e da aula. */
    String descreverFiltros(FiltrosRelatorio filtros);

    List<OpcaoFiltro> planos();

    List<OpcaoFiltro> personais();

    List<OpcaoFiltro> aulas();

    List<String> modalidades();
}
