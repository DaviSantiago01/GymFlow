package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.AreaEquipamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.DadosOperacionais;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.EquipamentoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.SituacaoEquipamento;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

public record RegistroEquipamento(Optional<RegistroEquipamentoIdentificador> identificador,
        EquipamentoIdentificador equipamento, LocalDate data, String responsavel, AreaEquipamento areaAnterior,
        AreaEquipamento areaNova, SituacaoEquipamento situacaoAnterior, SituacaoEquipamento situacaoNova,
        Optional<ResultadoInspecao> resultado, Optional<String> observacao)
        implements IAgregado<RegistroEquipamentoIdentificador> {
    public RegistroEquipamento {
        Objects.requireNonNull(identificador);
        Objects.requireNonNull(equipamento);
        Objects.requireNonNull(data);
        responsavel = DadosOperacionais.texto(responsavel, 100, "Responsável");
        Objects.requireNonNull(areaAnterior);
        Objects.requireNonNull(areaNova);
        Objects.requireNonNull(situacaoAnterior);
        Objects.requireNonNull(situacaoNova);
        Objects.requireNonNull(resultado);
        observacao = DadosOperacionais.textoOpcional(observacao, 800, "Observação");
    }

    @Override
    public Optional<RegistroEquipamentoIdentificador> getIdentificador() {
        return identificador;
    }
}
