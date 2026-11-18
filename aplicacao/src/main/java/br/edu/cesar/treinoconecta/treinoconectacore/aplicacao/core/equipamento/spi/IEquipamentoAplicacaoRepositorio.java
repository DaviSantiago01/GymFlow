package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.equipamento.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.equipamento.EquipamentoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.equipamento.EquipamentoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.EquipamentoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IEquipamentoAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<EquipamentoProjecao, EquipamentoConsulta> {
    EquipamentoProjecao projetar(EquipamentoIdentificador identificador);
}
