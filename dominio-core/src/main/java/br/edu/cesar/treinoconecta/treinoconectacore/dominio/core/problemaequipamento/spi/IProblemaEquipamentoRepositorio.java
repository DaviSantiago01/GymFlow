package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.ProblemaEquipamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.ProblemaEquipamentoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.EquipamentoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IProblemaEquipamentoRepositorio extends IAgregadoRepositorio<ProblemaEquipamentoIdentificador, ProblemaEquipamento> {
    EquipamentoIdentificador identificarEquipamento(ProblemaEquipamentoIdentificador problema);
    boolean existePorEquipamento(EquipamentoIdentificador equipamento);
    boolean existeAberto(EquipamentoIdentificador equipamento);
    boolean existeRiscoAberto(EquipamentoIdentificador equipamento);
}
