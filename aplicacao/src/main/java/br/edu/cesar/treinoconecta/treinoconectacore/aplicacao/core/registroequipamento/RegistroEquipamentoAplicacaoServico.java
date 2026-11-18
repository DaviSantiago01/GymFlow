package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroequipamento;

import java.util.Objects;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroequipamento.spi.IRegistroEquipamentoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.OperacaoEquipamentosAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.RegistroEquipamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.RegistroEquipamentoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.RegistroEquipamentoServico;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;

public final class RegistroEquipamentoAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<RegistroEquipamentoIdentificador, RegistroEquipamento,
        OperacaoEquipamentosAutorizacoes<RegistroEquipamento>, RegistroEquipamentoProjecao, RegistroEquipamentoConsulta> {
    private final IRegistroEquipamentoAplicacaoRepositorio repositorio;
    private final RegistroEquipamentoServico servico;

    public RegistroEquipamentoAplicacaoServico(IRegistroEquipamentoAplicacaoRepositorio repositorio, RegistroEquipamentoServico servico) {
        super(repositorio, servico);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.servico = Objects.requireNonNull(servico);
    }

    public RegistroEquipamentoProjecao projetar(RegistroEquipamentoIdentificador identificador) {
        servico.buscar(identificador);
        return repositorio.projetar(identificador);
    }
}
