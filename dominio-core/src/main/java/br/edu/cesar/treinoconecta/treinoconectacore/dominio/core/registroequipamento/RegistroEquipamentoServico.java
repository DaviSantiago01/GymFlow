package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.spi.IEquipamentoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.spi.IOrdemManutencaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.spi.IProblemaEquipamentoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.spi.IRegistroEquipamentoRepositorio;

public final class RegistroEquipamentoServico extends AgregadoServicoAbstrato<RegistroEquipamentoIdentificador,
        RegistroEquipamento, OperacaoEquipamentosAutorizacoes<RegistroEquipamento>> {
    public static final String INEXISTENTE = "Registro %s não encontrado";

    private final EquipamentoServico operacao;
    private final IEquipamentoRepositorio equipamentos;
    private final IProblemaEquipamentoRepositorio problemas;
    private final IOrdemManutencaoRepositorio ordens;

    public RegistroEquipamentoServico(ISegurancaServico seguranca, IRegistroEquipamentoRepositorio registros,
            EquipamentoServico operacao, IEquipamentoRepositorio equipamentos,
            IProblemaEquipamentoRepositorio problemas, IOrdemManutencaoRepositorio ordens) {
        super(seguranca, registros, "Registro %s já existe", INEXISTENTE);
        this.operacao = Objects.requireNonNull(operacao);
        this.equipamentos = Objects.requireNonNull(equipamentos);
        this.problemas = Objects.requireNonNull(problemas);
        this.ordens = Objects.requireNonNull(ordens);
    }

    @Override
    public OperacaoEquipamentosAutorizacoes<RegistroEquipamento> criarAutorizacoes(Optional<RegistroEquipamentoIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean administrador = getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name());
        return new OperacaoEquipamentosAutorizacoes<>(administrador, false);
    }

    public void movimentar(EquipamentoIdentificador identificador, AreaEquipamento area,
            SituacaoEquipamento situacao, Optional<String> observacao) {
        validarAutorizacao(Optional.empty(), OperacaoEquipamentosAutorizacoes::getOperar);
        var atual = equipamentos.bloquear(identificador);
        validarSituacao(atual, situacao, false);
        Validate.isTrue(atual.area() != area || atual.situacao() != situacao, "Informe uma mudança de área ou situação");
        operacao.registrarMudanca(atual, atual.mudar(area, situacao), operacao.hoje(), Optional.empty(), observacao);
    }

    public void inspecionar(EquipamentoIdentificador identificador, LocalDate data, ResultadoInspecao resultado,
            AreaEquipamento area, SituacaoEquipamento situacao, Optional<String> observacao) {
        validarAutorizacao(Optional.empty(), OperacaoEquipamentosAutorizacoes::getOperar);
        var atual = equipamentos.bloquear(identificador);
        DadosOperacionais.validarData(data, atual.ultimaInspecao().orElse(atual.dados().aquisicao()), operacao.hoje());
        Objects.requireNonNull(resultado);
        var novaSituacao = resultado == ResultadoInspecao.REPROVADO ? SituacaoEquipamento.INTERDITADO : situacao;
        if (novaSituacao == SituacaoEquipamento.DISPONIVEL && atual.situacao() != SituacaoEquipamento.DISPONIVEL) {
            Validate.isTrue(data.equals(operacao.hoje()), "A liberação exige inspeção na data de hoje");
        }
        validarSituacao(atual, novaSituacao, resultado != ResultadoInspecao.REPROVADO);
        operacao.registrarMudanca(atual, atual.inspecionar(area, novaSituacao, data), data,
                Optional.of(resultado), observacao);
    }

    private void validarSituacao(Equipamento atual, SituacaoEquipamento destino, boolean inspecaoAprovada) {
        atual.validarAtivo();
        Validate.isTrue(destino != SituacaoEquipamento.INATIVO, "Use a ação de inativação do cadastro");
        if (destino == SituacaoEquipamento.DISPONIVEL) {
            Validate.validState(atual.situacao() == destino || inspecaoAprovada, "A liberação exige inspeção aprovada");
            var identificador = atual.identificador().orElseThrow();
            Validate.validState(!ordens.existeAtivaNoEquipamento(identificador) && !problemas.existeRiscoAberto(identificador),
                    "O equipamento tem uma ordem ativa ou problema de segurança em aberto");
        }
    }
}
