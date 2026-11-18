package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.problemaequipamento.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.problemaequipamento.ProblemaEquipamentoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.problemaequipamento.ProblemaEquipamentoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.ProblemaEquipamentoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IProblemaEquipamentoAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<ProblemaEquipamentoProjecao, ProblemaEquipamentoConsulta> {
    ProblemaEquipamentoProjecao projetar(ProblemaEquipamentoIdentificador identificador);
}
