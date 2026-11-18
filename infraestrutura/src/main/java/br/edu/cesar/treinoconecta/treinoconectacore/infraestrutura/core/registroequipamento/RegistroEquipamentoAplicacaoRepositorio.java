package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.registroequipamento;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroequipamento.spi.IRegistroEquipamentoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.RegistroEquipamentoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.RegistroEquipamentoServico;
import dev.sauloaraujo.d5.framework.dominio.AgregadoInexistenteExcecao;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class RegistroEquipamentoAplicacaoRepositorio extends AgregadoAplicacaoRepositorioAbstrato<RegistroEquipamentoProjecao, RegistroEquipamentoOrdenacao, RegistroEquipamentoConsulta>
        implements IRegistroEquipamentoAplicacaoRepositorio {
    private final IRegistroEquipamentoRepositorioJpa jpa;

    public RegistroEquipamentoAplicacaoRepositorio(IRegistroEquipamentoRepositorioJpa jpa) {
        this.jpa = Objects.requireNonNull(jpa);
    }

    @Override
    protected Page<RegistroEquipamentoProjecao> pesquisar(RegistroEquipamentoConsulta consulta, Pageable pageable) {
        var pagina = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSort().and(Sort.by("identificador")));
        return jpa.pesquisar(consulta.getEquipamento().map(id -> Long.valueOf(id.toString())).orElse(null), pagina);
    }

    @Override
    protected String paraPropriedadeOrdenacao(RegistroEquipamentoOrdenacao ordenacao) {
        return "data";
    }

    @Override
    public RegistroEquipamentoProjecao projetar(RegistroEquipamentoIdentificador identificador) {
        return jpa.projetar(Long.valueOf(identificador.toString())).orElseThrow(
                () -> new AgregadoInexistenteExcecao(RegistroEquipamentoServico.INEXISTENTE.formatted(identificador)));
    }
}
