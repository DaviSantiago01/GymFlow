package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroequipamento;

import java.time.LocalDate;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public record RegistroEquipamentoProjecao(RegistroEquipamentoIdentificador identificador,
        EquipamentoIdentificador equipamento,
        LocalDate data,
        String responsavel,
        AreaEquipamento areaAnterior,
        AreaEquipamento areaNova,
        SituacaoEquipamento situacaoAnterior,
        SituacaoEquipamento situacaoNova,
        Optional<ResultadoInspecao> resultado,
        Optional<String> observacao,
        String equipamentoNome)
        implements IProjecao<RegistroEquipamento> {
    public RegistroEquipamentoProjecao(Long identificador,
            Long equipamento,
            LocalDate data,
            String responsavel,
            AreaEquipamento areaAnterior,
            AreaEquipamento areaNova,
            SituacaoEquipamento situacaoAnterior,
            SituacaoEquipamento situacaoNova,
            @Nullable ResultadoInspecao resultado,
            @Nullable String observacao,
            String equipamentoNome) {
        this(new RegistroEquipamentoIdentificador(identificador.toString()),
                new EquipamentoIdentificador(equipamento.toString()),
                data,
                responsavel,
                areaAnterior,
                areaNova,
                situacaoAnterior,
                situacaoNova,
                Optional.ofNullable(resultado),
                Optional.ofNullable(observacao),
                equipamentoNome);
    }

}
