package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.problemaequipamento;

import java.time.LocalDate;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public record ProblemaEquipamentoProjecao(ProblemaEquipamentoIdentificador identificador,
        EquipamentoIdentificador equipamento,
        String titulo,
        String relato,
        GravidadeProblema gravidade,
        boolean risco,
        Optional<String> evidencia,
        LocalDate dataRelato,
        String responsavel,
        SituacaoProblema situacao,
        Optional<LocalDate> dataEncerramento,
        Optional<String> desfecho,
        String equipamentoNome)
        implements IProjecao<ProblemaEquipamento> {
    public ProblemaEquipamentoProjecao(Long identificador,
            Long equipamento,
            String titulo,
            String relato,
            GravidadeProblema gravidade,
            Boolean risco,
            @Nullable String evidencia,
            LocalDate dataRelato,
            String responsavel,
            SituacaoProblema situacao,
            @Nullable LocalDate dataEncerramento,
            @Nullable String desfecho,
            String equipamentoNome) {
        this(new ProblemaEquipamentoIdentificador(identificador.toString()),
                new EquipamentoIdentificador(equipamento.toString()),
                titulo,
                relato,
                gravidade,
                risco,
                Optional.ofNullable(evidencia),
                dataRelato,
                responsavel,
                situacao,
                Optional.ofNullable(dataEncerramento),
                Optional.ofNullable(desfecho),
                equipamentoNome);
    }

}
