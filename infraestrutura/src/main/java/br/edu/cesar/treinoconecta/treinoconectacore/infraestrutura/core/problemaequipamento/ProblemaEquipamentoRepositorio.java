package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.problemaequipamento;

import java.util.Objects;
import java.util.Optional;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.spi.IProblemaEquipamentoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.equipamento.IEquipamentoRepositorioJpa;
import dev.sauloaraujo.d5.framework.dominio.AgregadoInexistenteExcecao;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class ProblemaEquipamentoRepositorio extends AgregadoRepositorioAbstrato<ProblemaEquipamentoIdentificador, ProblemaEquipamento, Long, ProblemaEquipamentoJpa>
        implements IProblemaEquipamentoRepositorio {
    private final IProblemaEquipamentoRepositorioJpa jpa;
    private final IEquipamentoRepositorioJpa referencias;

    public ProblemaEquipamentoRepositorio(IProblemaEquipamentoRepositorioJpa jpa, IEquipamentoRepositorioJpa referencias) {
        super(jpa, ProblemaEquipamentoServico.INEXISTENTE);
        this.jpa = Objects.requireNonNull(jpa);
        this.referencias = Objects.requireNonNull(referencias);
    }

    @Override
    protected Long paraIdentificadorJpa(ProblemaEquipamentoIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected ProblemaEquipamentoJpa paraJpa(ProblemaEquipamento agregado) {
        return new ProblemaEquipamentoJpa(agregado.identificador().map(this::paraIdentificadorJpa).orElse(null),
                referencias.getReferenceById(Long.valueOf(agregado.equipamento().toString())),
                agregado.dados().titulo(),
                agregado.dados().relato(),
                agregado.dados().gravidade(),
                agregado.dados().risco(),
                agregado.dados().evidencia().orElse(null),
                agregado.dados().dataRelato(),
                agregado.responsavel(),
                agregado.situacao(),
                agregado.dataEncerramento().orElse(null),
                agregado.desfecho().orElse(null));
    }

    @Override
    protected ProblemaEquipamento paraAgregado(ProblemaEquipamentoJpa entidade) {
        return new ProblemaEquipamento(Optional.of(new ProblemaEquipamentoIdentificador(entidade.getIdentificador().toString())),
                new EquipamentoIdentificador(entidade.getEquipamento().getIdentificador().toString()),
                new DadosProblema(entidade.getTitulo(), entidade.getRelato(), entidade.getGravidade(), entidade.getRisco(), Optional.ofNullable(entidade.getEvidencia()), entidade.getDataRelato()),
                entidade.getResponsavel(),
                entidade.getSituacao(),
                Optional.ofNullable(entidade.getDataEncerramento()),
                Optional.ofNullable(entidade.getDesfecho()));
    }

    @Override
    public EquipamentoIdentificador identificarEquipamento(ProblemaEquipamentoIdentificador problema) {
        return jpa.identificarEquipamento(paraIdentificadorJpa(problema))
                .map(id -> new EquipamentoIdentificador(id.toString()))
                .orElseThrow(() -> new AgregadoInexistenteExcecao(ProblemaEquipamentoServico.INEXISTENTE.formatted(problema)));
    }

    @Override
    public boolean existePorEquipamento(EquipamentoIdentificador equipamento) {
        return jpa.existsByEquipamentoIdentificador(Long.valueOf(equipamento.toString()));
    }

    @Override
    public boolean existeAberto(EquipamentoIdentificador equipamento) {
        return jpa.existsByEquipamentoIdentificadorAndSituacao(Long.valueOf(equipamento.toString()), SituacaoProblema.ABERTO);
    }

    @Override
    public boolean existeRiscoAberto(EquipamentoIdentificador equipamento) {
        return jpa.existeRisco(Long.valueOf(equipamento.toString()), SituacaoProblema.ABERTO, GravidadeProblema.CRITICA);
    }
}
