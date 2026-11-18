package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.ordemmanutencao;

import java.util.Objects;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.ordemmanutencao.spi.IOrdemManutencaoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.OperacaoEquipamentosAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.OrdemManutencao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.OrdemManutencaoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.OrdemManutencaoServico;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;

public final class OrdemManutencaoAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<OrdemManutencaoIdentificador, OrdemManutencao,
        OperacaoEquipamentosAutorizacoes<OrdemManutencao>, OrdemManutencaoProjecao, OrdemManutencaoConsulta> {
    private final IOrdemManutencaoAplicacaoRepositorio repositorio;
    private final OrdemManutencaoServico servico;

    public OrdemManutencaoAplicacaoServico(IOrdemManutencaoAplicacaoRepositorio repositorio, OrdemManutencaoServico servico) {
        super(repositorio, servico);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.servico = Objects.requireNonNull(servico);
    }

    public OrdemManutencaoProjecao projetar(OrdemManutencaoIdentificador identificador) {
        servico.buscar(identificador);
        return repositorio.projetar(identificador);
    }
}
