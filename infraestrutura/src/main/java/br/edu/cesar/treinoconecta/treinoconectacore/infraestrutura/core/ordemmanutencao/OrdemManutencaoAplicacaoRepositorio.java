package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.ordemmanutencao;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.ordemmanutencao.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.ordemmanutencao.spi.IOrdemManutencaoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.OrdemManutencaoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.OrdemManutencaoServico;
import dev.sauloaraujo.d5.framework.dominio.AgregadoInexistenteExcecao;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class OrdemManutencaoAplicacaoRepositorio extends AgregadoAplicacaoRepositorioAbstrato<OrdemManutencaoProjecao, OrdemManutencaoOrdenacao, OrdemManutencaoConsulta>
        implements IOrdemManutencaoAplicacaoRepositorio {
    private final IOrdemManutencaoRepositorioJpa jpa;

    public OrdemManutencaoAplicacaoRepositorio(IOrdemManutencaoRepositorioJpa jpa) {
        this.jpa = Objects.requireNonNull(jpa);
    }

    @Override
    protected Page<OrdemManutencaoProjecao> pesquisar(OrdemManutencaoConsulta consulta, Pageable pageable) {
        var pagina = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSort().and(Sort.by("identificador")));
        return jpa.pesquisar(consulta.getProblema().map(id -> Long.valueOf(id.toString())).orElse(null),
                consulta.getSituacao().orElse(null), pagina);
    }

    @Override
    protected String paraPropriedadeOrdenacao(OrdemManutencaoOrdenacao ordenacao) {
        return "abertura";
    }

    @Override
    public OrdemManutencaoProjecao projetar(OrdemManutencaoIdentificador identificador) {
        return jpa.projetar(Long.valueOf(identificador.toString())).orElseThrow(
                () -> new AgregadoInexistenteExcecao(OrdemManutencaoServico.INEXISTENTE.formatted(identificador)));
    }
}
