package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.DadosOperacionais;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.ProblemaEquipamentoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

public record OrdemManutencao(Optional<OrdemManutencaoIdentificador> identificador,
        ProblemaEquipamentoIdentificador problema, DadosOrdem dados, LocalDate abertura, String responsavel,
        SituacaoOrdem situacao, Optional<LocalDate> inicio, Optional<LocalDate> termino, Optional<BigDecimal> custoReal,
        Optional<String> servicoExecutado, Optional<String> justificativa)
        implements IAgregado<OrdemManutencaoIdentificador> {
    public OrdemManutencao {
        Objects.requireNonNull(identificador);
        Objects.requireNonNull(problema);
        Objects.requireNonNull(dados);
        Objects.requireNonNull(abertura);
        responsavel = DadosOperacionais.texto(responsavel, 100, "Responsável");
        Objects.requireNonNull(situacao);
        Objects.requireNonNull(inicio);
        Objects.requireNonNull(termino);
        custoReal = DadosOrdem.validarCusto(custoReal);
        servicoExecutado = DadosOperacionais.textoOpcional(servicoExecutado, 800, "Serviço executado");
        justificativa = DadosOperacionais.textoOpcional(justificativa, 800, "Justificativa");
    }

    public static OrdemManutencao abrir(ProblemaEquipamentoIdentificador problema, DadosOrdem dados,
            LocalDate data, String responsavel) {
        return new OrdemManutencao(Optional.empty(), problema, dados, data, responsavel, SituacaoOrdem.ABERTA,
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Override
    public Optional<OrdemManutencaoIdentificador> getIdentificador() {
        return identificador;
    }

    public OrdemManutencao editar(DadosOrdem novosDados) {
        validarAtiva();
        return new OrdemManutencao(identificador, problema, novosDados, abertura, responsavel, situacao,
                inicio, termino, custoReal, servicoExecutado, justificativa);
    }

    public OrdemManutencao iniciar(LocalDate data, LocalDate hoje) {
        DadosOperacionais.validarData(data, abertura, hoje);
        Validate.validState(situacao == SituacaoOrdem.ABERTA, "Somente uma ordem aberta pode ser iniciada");
        return new OrdemManutencao(identificador, problema, dados, abertura, responsavel, SituacaoOrdem.EM_ANDAMENTO,
                Optional.of(data), termino, custoReal, servicoExecutado, justificativa);
    }

    public OrdemManutencao concluir(LocalDate data, String servico, Optional<BigDecimal> custo, LocalDate hoje) {
        var descricao = DadosOperacionais.texto(servico, 800, "Serviço executado");
        var valor = DadosOrdem.validarCusto(custo);
        Validate.validState(situacao == SituacaoOrdem.EM_ANDAMENTO, "Somente uma ordem em andamento pode ser concluída");
        DadosOperacionais.validarData(data, inicio.orElseThrow(), hoje);
        return new OrdemManutencao(identificador, problema, dados, abertura, responsavel, SituacaoOrdem.CONCLUIDA,
                inicio, Optional.of(data), valor, Optional.of(descricao), Optional.empty());
    }

    public OrdemManutencao cancelar(LocalDate hoje, String motivo) {
        var descricao = DadosOperacionais.texto(motivo, 800, "Justificativa");
        validarAtiva();
        return new OrdemManutencao(identificador, problema, dados, abertura, responsavel, SituacaoOrdem.CANCELADA,
                inicio, Optional.of(hoje), custoReal, servicoExecutado, Optional.of(descricao));
    }

    private void validarAtiva() {
        Validate.validState(situacao == SituacaoOrdem.ABERTA || situacao == SituacaoOrdem.EM_ANDAMENTO,
                "A ordem já está encerrada");
    }
}
