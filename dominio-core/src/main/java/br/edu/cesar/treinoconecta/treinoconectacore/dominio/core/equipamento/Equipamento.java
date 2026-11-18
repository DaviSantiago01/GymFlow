package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import dev.sauloaraujo.d5.framework.dominio.IAgregado;

public record Equipamento(Optional<EquipamentoIdentificador> identificador, DadosEquipamento dados,
        AreaEquipamento area, SituacaoEquipamento situacao, Optional<LocalDate> ultimaInspecao)
        implements IAgregado<EquipamentoIdentificador> {
    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    public Equipamento {
        Objects.requireNonNull(identificador);
        Objects.requireNonNull(dados);
        Objects.requireNonNull(area);
        Objects.requireNonNull(situacao);
        Objects.requireNonNull(ultimaInspecao);
    }

    public static Equipamento cadastrar(DadosEquipamento dados, AreaEquipamento area) {
        return new Equipamento(Optional.empty(), dados, area, SituacaoEquipamento.DISPONIVEL, Optional.empty());
    }

    @Override
    public Optional<EquipamentoIdentificador> getIdentificador() {
        return identificador;
    }

    public void validarAtivo() {
        Validate.validState(situacao != SituacaoEquipamento.INATIVO, "O equipamento está inativo");
    }

    public Equipamento editar(DadosEquipamento novosDados) {
        validarAtivo();
        return new Equipamento(identificador, novosDados, area, situacao, ultimaInspecao);
    }

    public Equipamento mudar(AreaEquipamento novaArea, SituacaoEquipamento novaSituacao) {
        validarAtivo();
        return new Equipamento(identificador, dados, novaArea, novaSituacao, ultimaInspecao);
    }

    public Equipamento inspecionar(AreaEquipamento novaArea, SituacaoEquipamento novaSituacao, LocalDate data) {
        validarAtivo();
        return new Equipamento(identificador, dados, novaArea, novaSituacao, Optional.of(data));
    }

    public LocalDate proximaInspecao() {
        return calcularProximaInspecao(ultimaInspecao, dados.aquisicao(), dados.periodicidade());
    }

    public static LocalDate calcularProximaInspecao(Optional<LocalDate> ultima, LocalDate aquisicao, int periodicidade) {
        return ultima.orElse(aquisicao).plusDays(periodicidade);
    }

    public String prazoInspecao(LocalDate hoje) {
        return classificarPrazo(proximaInspecao(), hoje);
    }

    public static String classificarPrazo(LocalDate proxima, LocalDate hoje) {
        Objects.requireNonNull(proxima);
        Objects.requireNonNull(hoje);
        if (proxima.isBefore(hoje)) {
            return "ATRASADA";
        }
        return proxima.isAfter(hoje.plusDays(7)) ? "EM_DIA" : "PROXIMA";
    }
}
