package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.OrdemManutencao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.OrdemManutencaoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.EquipamentoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.ProblemaEquipamentoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IOrdemManutencaoRepositorio extends IAgregadoRepositorio<OrdemManutencaoIdentificador, OrdemManutencao> {
    ProblemaEquipamentoIdentificador identificarProblema(OrdemManutencaoIdentificador ordem);
    boolean existeAtiva(ProblemaEquipamentoIdentificador problema);
    boolean existeAtivaNoEquipamento(EquipamentoIdentificador equipamento);
}
