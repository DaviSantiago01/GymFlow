package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.equipamento;

import java.util.Objects;
import java.util.Optional;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.spi.IEquipamentoRepositorio;

import dev.sauloaraujo.d5.framework.dominio.AgregadoInexistenteExcecao;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class EquipamentoRepositorio extends AgregadoRepositorioAbstrato<EquipamentoIdentificador, Equipamento, Long, EquipamentoJpa>
        implements IEquipamentoRepositorio {
    private final IEquipamentoRepositorioJpa jpa;

    public EquipamentoRepositorio(IEquipamentoRepositorioJpa jpa) {
        super(jpa, EquipamentoServico.INEXISTENTE);
        this.jpa = Objects.requireNonNull(jpa);
    }

    @Override
    protected Long paraIdentificadorJpa(EquipamentoIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected EquipamentoJpa paraJpa(Equipamento agregado) {
        return new EquipamentoJpa(agregado.identificador().map(this::paraIdentificadorJpa).orElse(null),
                agregado.dados().patrimonio(),
                agregado.dados().serie().orElse(null),
                agregado.dados().nome(),
                agregado.dados().tipo(),
                agregado.dados().marca(),
                agregado.dados().modelo(),
                agregado.dados().aquisicao(),
                agregado.area(),
                agregado.situacao(),
                agregado.dados().periodicidade(),
                agregado.ultimaInspecao().orElse(null));
    }

    @Override
    protected Equipamento paraAgregado(EquipamentoJpa entidade) {
        return new Equipamento(Optional.of(new EquipamentoIdentificador(entidade.getIdentificador().toString())),
                new DadosEquipamento(entidade.getPatrimonio(), Optional.ofNullable(entidade.getSerie()), entidade.getNome(), entidade.getTipo(), entidade.getMarca(), entidade.getModelo(), entidade.getAquisicao(), entidade.getPeriodicidade()),
                entidade.getArea(),
                entidade.getSituacao(),
                Optional.ofNullable(entidade.getUltimaInspecao()));
    }

    @Override
    public Equipamento bloquear(EquipamentoIdentificador identificador) {
        return jpa.bloquear(paraIdentificadorJpa(identificador)).map(this::paraAgregado)
                .orElseThrow(() -> new AgregadoInexistenteExcecao(EquipamentoServico.INEXISTENTE.formatted(identificador)));
    }

    @Override
    public boolean existePatrimonio(String patrimonio, Optional<EquipamentoIdentificador> ignorado) {
        return jpa.existePatrimonio(patrimonio, ignorado.map(this::paraIdentificadorJpa).orElse(null));
    }

    @Override
    public boolean existeSerie(String serie, Optional<EquipamentoIdentificador> ignorado) {
        return jpa.existeSerie(serie, ignorado.map(this::paraIdentificadorJpa).orElse(null));
    }
}
