package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.problemaequipamento;

import java.util.Objects;
import java.util.Optional;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class ProblemaEquipamentoConsulta extends Consulta<ProblemaEquipamentoOrdenacao> {
    private final Optional<EquipamentoIdentificador> equipamento;
    private final Optional<SituacaoProblema> situacao;
    private final Optional<GravidadeProblema> gravidade;

    public ProblemaEquipamentoConsulta(Optional<ProblemaEquipamentoOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina,
            Optional<EquipamentoIdentificador> equipamento, Optional<SituacaoProblema> situacao, Optional<GravidadeProblema> gravidade) {
        super(ordenacao.or(() -> Optional.of(ProblemaEquipamentoOrdenacao.DATA_RELATO)), descendente, indicePagina, tamanhoPagina);
        this.equipamento = Objects.requireNonNull(equipamento);
        this.situacao = Objects.requireNonNull(situacao);
        this.gravidade = Objects.requireNonNull(gravidade);
    }

    public Optional<EquipamentoIdentificador> getEquipamento() {
        return equipamento;
    }

    public Optional<SituacaoProblema> getSituacao() {
        return situacao;
    }

    public Optional<GravidadeProblema> getGravidade() {
        return gravidade;
    }
}
