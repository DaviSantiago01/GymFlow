package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.problemaequipamento;

import java.util.Objects;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.problemaequipamento.spi.IProblemaEquipamentoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.OperacaoEquipamentosAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.ProblemaEquipamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.ProblemaEquipamentoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.ProblemaEquipamentoServico;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;

public final class ProblemaEquipamentoAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<ProblemaEquipamentoIdentificador, ProblemaEquipamento,
        OperacaoEquipamentosAutorizacoes<ProblemaEquipamento>, ProblemaEquipamentoProjecao, ProblemaEquipamentoConsulta> {
    private final IProblemaEquipamentoAplicacaoRepositorio repositorio;
    private final ProblemaEquipamentoServico servico;

    public ProblemaEquipamentoAplicacaoServico(IProblemaEquipamentoAplicacaoRepositorio repositorio, ProblemaEquipamentoServico servico) {
        super(repositorio, servico);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.servico = Objects.requireNonNull(servico);
    }

    public ProblemaEquipamentoProjecao projetar(ProblemaEquipamentoIdentificador identificador) {
        servico.buscar(identificador);
        return repositorio.projetar(identificador);
    }
}
