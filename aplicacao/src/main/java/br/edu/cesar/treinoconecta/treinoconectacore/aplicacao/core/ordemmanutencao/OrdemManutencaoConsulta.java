package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.ordemmanutencao;

import java.util.Objects;
import java.util.Optional;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.*;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class OrdemManutencaoConsulta extends Consulta<OrdemManutencaoOrdenacao> {
    private final Optional<ProblemaEquipamentoIdentificador> problema;
    private final Optional<SituacaoOrdem> situacao;

    public OrdemManutencaoConsulta(Optional<OrdemManutencaoOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina,
            Optional<ProblemaEquipamentoIdentificador> problema, Optional<SituacaoOrdem> situacao) {
        super(ordenacao.or(() -> Optional.of(OrdemManutencaoOrdenacao.ABERTURA)), descendente, indicePagina, tamanhoPagina);
        this.problema = Objects.requireNonNull(problema);
        this.situacao = Objects.requireNonNull(situacao);
    }

    public Optional<ProblemaEquipamentoIdentificador> getProblema() {
        return problema;
    }

    public Optional<SituacaoOrdem> getSituacao() {
        return situacao;
    }
}
