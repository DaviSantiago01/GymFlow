package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento;

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

public final class ProblemaEquipamentoServico extends AgregadoServicoAbstrato<ProblemaEquipamentoIdentificador,
        ProblemaEquipamento, OperacaoEquipamentosAutorizacoes<ProblemaEquipamento>> {
    public static final String INEXISTENTE = "Registro %s não encontrado";

    private final IProblemaEquipamentoRepositorio problemas;
    private final IEquipamentoRepositorio equipamentos;
    private final IOrdemManutencaoRepositorio ordens;
    private final EquipamentoServico operacao;

    public ProblemaEquipamentoServico(ISegurancaServico seguranca, IProblemaEquipamentoRepositorio problemas,
            IEquipamentoRepositorio equipamentos, IOrdemManutencaoRepositorio ordens, EquipamentoServico operacao) {
        super(seguranca, problemas, "Problema %s já existe", INEXISTENTE);
        this.problemas = Objects.requireNonNull(problemas);
        this.equipamentos = Objects.requireNonNull(equipamentos);
        this.ordens = Objects.requireNonNull(ordens);
        this.operacao = Objects.requireNonNull(operacao);
    }

    @Override
    public OperacaoEquipamentosAutorizacoes<ProblemaEquipamento> criarAutorizacoes(Optional<ProblemaEquipamentoIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean administrador = getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name());
        return new OperacaoEquipamentosAutorizacoes<>(administrador, false);
    }

    public ProblemaEquipamento registrar(EquipamentoIdentificador identificador, DadosProblema dados) {
        validarAutorizacao(Optional.empty(), OperacaoEquipamentosAutorizacoes::getOperar);
        var equipamento = equipamentos.bloquear(identificador);
        equipamento.validarAtivo();
        DadosOperacionais.validarData(dados.dataRelato(), equipamento.dados().aquisicao(), operacao.hoje());
        var problema = problemas.incluir(ProblemaEquipamento.registrar(identificador, dados, getSegurancaServico().getUsuario()));
        interditarSeNecessario(equipamento, dados);
        return problema;
    }

    public void editar(ProblemaEquipamentoIdentificador identificador, DadosProblema dados) {
        validarAutorizacao(Optional.of(identificador), OperacaoEquipamentosAutorizacoes::getOperar);
        var equipamento = bloquearEquipamento(identificador);
        var problema = problemas.buscarPorIdentificador(identificador);
        problemas.alterar(problema.editar(dados));
        interditarSeNecessario(equipamento, dados);
    }

    public void resolver(ProblemaEquipamentoIdentificador identificador, LocalDate data, String solucao) {
        var problema = prepararEncerramento(identificador);
        problemas.alterar(problema.resolver(data, solucao, operacao.hoje()));
    }

    public void cancelar(ProblemaEquipamentoIdentificador identificador, String justificativa) {
        var problema = prepararEncerramento(identificador);
        problemas.alterar(problema.cancelar(operacao.hoje(), justificativa));
    }

    private Equipamento bloquearEquipamento(ProblemaEquipamentoIdentificador identificador) {
        return equipamentos.bloquear(problemas.identificarEquipamento(identificador));
    }

    private ProblemaEquipamento prepararEncerramento(ProblemaEquipamentoIdentificador identificador) {
        validarAutorizacao(Optional.of(identificador), OperacaoEquipamentosAutorizacoes::getOperar);
        bloquearEquipamento(identificador);
        Validate.validState(!ordens.existeAtiva(identificador), "O problema tem ordem de manutenção ativa");
        return problemas.buscarPorIdentificador(identificador);
    }

    private void interditarSeNecessario(Equipamento equipamento, DadosProblema dados) {
        if (dados.exigeInterdicao() && equipamento.situacao() != SituacaoEquipamento.INTERDITADO) {
            operacao.registrarMudanca(equipamento, equipamento.mudar(equipamento.area(), SituacaoEquipamento.INTERDITADO),
                    operacao.hoje(), Optional.empty(), Optional.of("Interdição por problema de segurança"));
        }
    }
}
