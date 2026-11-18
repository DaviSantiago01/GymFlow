package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.equipamento;

import java.time.LocalDate;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public record EquipamentoProjecao(EquipamentoIdentificador identificador,
        String patrimonio,
        Optional<String> serie,
        String nome,
        TipoEquipamento tipo,
        String marca,
        String modelo,
        LocalDate aquisicao,
        AreaEquipamento area,
        SituacaoEquipamento situacao,
        int periodicidade,
        Optional<LocalDate> ultimaInspecao)
        implements IProjecao<Equipamento> {
    public EquipamentoProjecao(Long identificador,
            String patrimonio,
            @Nullable String serie,
            String nome,
            TipoEquipamento tipo,
            String marca,
            String modelo,
            LocalDate aquisicao,
            AreaEquipamento area,
            SituacaoEquipamento situacao,
            Integer periodicidade,
            @Nullable LocalDate ultimaInspecao) {
        this(new EquipamentoIdentificador(identificador.toString()),
                patrimonio,
                Optional.ofNullable(serie),
                nome,
                tipo,
                marca,
                modelo,
                aquisicao,
                area,
                situacao,
                periodicidade,
                Optional.ofNullable(ultimaInspecao));
    }

    public LocalDate getProximaInspecao() {
        return Equipamento.calcularProximaInspecao(ultimaInspecao, aquisicao, periodicidade);
    }

    public String getPrazoInspecao() {
        return Equipamento.classificarPrazo(getProximaInspecao(), LocalDate.now(Equipamento.FUSO));
    }
}
