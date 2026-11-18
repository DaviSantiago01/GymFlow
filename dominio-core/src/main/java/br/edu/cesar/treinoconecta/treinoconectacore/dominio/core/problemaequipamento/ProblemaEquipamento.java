package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.DadosOperacionais;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.EquipamentoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

public record ProblemaEquipamento(Optional<ProblemaEquipamentoIdentificador> identificador,
        EquipamentoIdentificador equipamento, DadosProblema dados, String responsavel, SituacaoProblema situacao,
        Optional<LocalDate> dataEncerramento, Optional<String> desfecho)
        implements IAgregado<ProblemaEquipamentoIdentificador> {
    public ProblemaEquipamento {
        Objects.requireNonNull(identificador);
        Objects.requireNonNull(equipamento);
        Objects.requireNonNull(dados);
        responsavel = DadosOperacionais.texto(responsavel, 100, "Responsável");
        Objects.requireNonNull(situacao);
        Objects.requireNonNull(dataEncerramento);
        desfecho = DadosOperacionais.textoOpcional(desfecho, 800, "Desfecho");
    }

    public static ProblemaEquipamento registrar(EquipamentoIdentificador equipamento, DadosProblema dados,
            String responsavel) {
        return new ProblemaEquipamento(Optional.empty(), equipamento, dados, responsavel, SituacaoProblema.ABERTO,
                Optional.empty(), Optional.empty());
    }

    @Override
    public Optional<ProblemaEquipamentoIdentificador> getIdentificador() {
        return identificador;
    }

    public void validarAberto() {
        Validate.validState(situacao == SituacaoProblema.ABERTO, "O problema deve estar em aberto");
    }

    public ProblemaEquipamento editar(DadosProblema novosDados) {
        Validate.isTrue(dados.dataRelato().equals(novosDados.dataRelato()), "A data original do relato é preservada");
        validarAberto();
        return new ProblemaEquipamento(identificador, equipamento, novosDados, responsavel, situacao,
                dataEncerramento, desfecho);
    }

    public ProblemaEquipamento resolver(LocalDate data, String solucao, LocalDate hoje) {
        return encerrar(data, solucao, hoje, SituacaoProblema.RESOLVIDO, "Solução");
    }

    public ProblemaEquipamento cancelar(LocalDate hoje, String justificativa) {
        return encerrar(hoje, justificativa, hoje, SituacaoProblema.CANCELADO, "Justificativa");
    }

    private ProblemaEquipamento encerrar(LocalDate data, String descricao, LocalDate hoje,
            SituacaoProblema estadoFinal, String campo) {
        var texto = DadosOperacionais.texto(descricao, 800, campo);
        DadosOperacionais.validarData(data, dados.dataRelato(), hoje);
        validarAberto();
        return new ProblemaEquipamento(identificador, equipamento, dados, responsavel, estadoFinal,
                Optional.of(data), Optional.of(texto));
    }
}
