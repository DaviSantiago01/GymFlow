package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.registroequipamento;

import java.util.Objects;
import java.util.Optional;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.spi.IRegistroEquipamentoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.equipamento.IEquipamentoRepositorioJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class RegistroEquipamentoRepositorio extends AgregadoRepositorioAbstrato<RegistroEquipamentoIdentificador, RegistroEquipamento, Long, RegistroEquipamentoJpa>
        implements IRegistroEquipamentoRepositorio {
    private final IRegistroEquipamentoRepositorioJpa jpa;
    private final IEquipamentoRepositorioJpa referencias;

    public RegistroEquipamentoRepositorio(IRegistroEquipamentoRepositorioJpa jpa, IEquipamentoRepositorioJpa referencias) {
        super(jpa, RegistroEquipamentoServico.INEXISTENTE);
        this.jpa = Objects.requireNonNull(jpa);
        this.referencias = Objects.requireNonNull(referencias);
    }

    @Override
    protected Long paraIdentificadorJpa(RegistroEquipamentoIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected RegistroEquipamentoJpa paraJpa(RegistroEquipamento agregado) {
        return new RegistroEquipamentoJpa(agregado.identificador().map(this::paraIdentificadorJpa).orElse(null),
                referencias.getReferenceById(Long.valueOf(agregado.equipamento().toString())),
                agregado.data(),
                agregado.responsavel(),
                agregado.areaAnterior(),
                agregado.areaNova(),
                agregado.situacaoAnterior(),
                agregado.situacaoNova(),
                agregado.resultado().orElse(null),
                agregado.observacao().orElse(null));
    }

    @Override
    protected RegistroEquipamento paraAgregado(RegistroEquipamentoJpa entidade) {
        return new RegistroEquipamento(Optional.of(new RegistroEquipamentoIdentificador(entidade.getIdentificador().toString())),
                new EquipamentoIdentificador(entidade.getEquipamento().getIdentificador().toString()),
                entidade.getData(),
                entidade.getResponsavel(),
                entidade.getAreaAnterior(),
                entidade.getAreaNova(),
                entidade.getSituacaoAnterior(),
                entidade.getSituacaoNova(),
                Optional.ofNullable(entidade.getResultado()),
                Optional.ofNullable(entidade.getObservacao()));
    }

    @Override
    public boolean existePorEquipamento(EquipamentoIdentificador equipamento) {
        return jpa.existsByEquipamentoIdentificador(Long.valueOf(equipamento.toString()));
    }
}
