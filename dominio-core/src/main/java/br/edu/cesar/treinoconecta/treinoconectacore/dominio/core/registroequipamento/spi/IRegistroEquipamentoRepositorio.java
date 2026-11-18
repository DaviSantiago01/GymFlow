package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.RegistroEquipamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.RegistroEquipamentoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.EquipamentoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IRegistroEquipamentoRepositorio extends IAgregadoRepositorio<RegistroEquipamentoIdentificador, RegistroEquipamento> {
    boolean existePorEquipamento(EquipamentoIdentificador equipamento);
}
