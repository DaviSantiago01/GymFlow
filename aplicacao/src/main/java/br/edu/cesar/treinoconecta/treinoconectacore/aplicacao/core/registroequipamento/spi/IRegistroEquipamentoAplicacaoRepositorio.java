package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroequipamento.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroequipamento.RegistroEquipamentoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroequipamento.RegistroEquipamentoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.RegistroEquipamentoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IRegistroEquipamentoAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<RegistroEquipamentoProjecao, RegistroEquipamentoConsulta> {
    RegistroEquipamentoProjecao projetar(RegistroEquipamentoIdentificador identificador);
}
