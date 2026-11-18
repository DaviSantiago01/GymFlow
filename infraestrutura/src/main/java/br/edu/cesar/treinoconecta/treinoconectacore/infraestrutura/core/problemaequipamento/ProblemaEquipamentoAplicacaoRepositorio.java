package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.problemaequipamento;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.problemaequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.problemaequipamento.spi.IProblemaEquipamentoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.ProblemaEquipamentoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.ProblemaEquipamentoServico;
import dev.sauloaraujo.d5.framework.dominio.AgregadoInexistenteExcecao;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class ProblemaEquipamentoAplicacaoRepositorio extends AgregadoAplicacaoRepositorioAbstrato<ProblemaEquipamentoProjecao, ProblemaEquipamentoOrdenacao, ProblemaEquipamentoConsulta>
        implements IProblemaEquipamentoAplicacaoRepositorio {
    private final IProblemaEquipamentoRepositorioJpa jpa;

    public ProblemaEquipamentoAplicacaoRepositorio(IProblemaEquipamentoRepositorioJpa jpa) {
        this.jpa = Objects.requireNonNull(jpa);
    }

    @Override
    protected Page<ProblemaEquipamentoProjecao> pesquisar(ProblemaEquipamentoConsulta consulta, Pageable pageable) {
        var pagina = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSort().and(Sort.by("identificador")));
        return jpa.pesquisar(consulta.getEquipamento().map(id -> Long.valueOf(id.toString())).orElse(null),
                consulta.getSituacao().orElse(null),
                consulta.getGravidade().orElse(null), pagina);
    }

    @Override
    protected String paraPropriedadeOrdenacao(ProblemaEquipamentoOrdenacao ordenacao) {
        return "dataRelato";
    }

    @Override
    public ProblemaEquipamentoProjecao projetar(ProblemaEquipamentoIdentificador identificador) {
        return jpa.projetar(Long.valueOf(identificador.toString())).orElseThrow(
                () -> new AgregadoInexistenteExcecao(ProblemaEquipamentoServico.INEXISTENTE.formatted(identificador)));
    }
}
