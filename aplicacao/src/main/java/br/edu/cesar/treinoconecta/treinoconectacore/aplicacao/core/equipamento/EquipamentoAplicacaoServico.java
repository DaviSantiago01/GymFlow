package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.equipamento;

import java.util.Objects;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.equipamento.spi.IEquipamentoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.OperacaoEquipamentosAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.Equipamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.EquipamentoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.EquipamentoServico;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;

public final class EquipamentoAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<EquipamentoIdentificador, Equipamento,
        OperacaoEquipamentosAutorizacoes<Equipamento>, EquipamentoProjecao, EquipamentoConsulta> {
    private final IEquipamentoAplicacaoRepositorio repositorio;
    private final EquipamentoServico servico;

    public EquipamentoAplicacaoServico(IEquipamentoAplicacaoRepositorio repositorio, EquipamentoServico servico) {
        super(repositorio, servico);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.servico = Objects.requireNonNull(servico);
    }

    public EquipamentoProjecao projetar(EquipamentoIdentificador identificador) {
        servico.buscar(identificador);
        return repositorio.projetar(identificador);
    }
}
