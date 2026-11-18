package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.equipamento;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.equipamento.spi.IEquipamentoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.EquipamentoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.EquipamentoServico;
import dev.sauloaraujo.d5.framework.dominio.AgregadoInexistenteExcecao;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class EquipamentoAplicacaoRepositorio extends AgregadoAplicacaoRepositorioAbstrato<EquipamentoProjecao, EquipamentoOrdenacao, EquipamentoConsulta>
        implements IEquipamentoAplicacaoRepositorio {
    private final IEquipamentoRepositorioJpa jpa;

    public EquipamentoAplicacaoRepositorio(IEquipamentoRepositorioJpa jpa) {
        this.jpa = Objects.requireNonNull(jpa);
    }

    @Override
    protected Page<EquipamentoProjecao> pesquisar(EquipamentoConsulta consulta, Pageable pageable) {
        var pagina = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSort().and(Sort.by("identificador")));
        return jpa.pesquisar(paraLike(consulta.getTermo()),
                consulta.getSituacao().orElse(null),
                consulta.getArea().orElse(null),
                consulta.getTipo().orElse(null), pagina);
    }

    @Override
    protected String paraPropriedadeOrdenacao(EquipamentoOrdenacao ordenacao) {
        return "nome";
    }

    @Override
    public EquipamentoProjecao projetar(EquipamentoIdentificador identificador) {
        return jpa.projetar(Long.valueOf(identificador.toString())).orElseThrow(
                () -> new AgregadoInexistenteExcecao(EquipamentoServico.INEXISTENTE.formatted(identificador)));
    }
}
