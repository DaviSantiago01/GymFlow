package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroequipamento;

import java.util.Objects;
import java.util.Optional;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class RegistroEquipamentoConsulta extends Consulta<RegistroEquipamentoOrdenacao> {
    private final Optional<EquipamentoIdentificador> equipamento;

    public RegistroEquipamentoConsulta(Optional<RegistroEquipamentoOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina,
            Optional<EquipamentoIdentificador> equipamento) {
        super(ordenacao.or(() -> Optional.of(RegistroEquipamentoOrdenacao.DATA)), descendente, indicePagina, tamanhoPagina);
        this.equipamento = Objects.requireNonNull(equipamento);
    }

    public Optional<EquipamentoIdentificador> getEquipamento() {
        return equipamento;
    }
}
