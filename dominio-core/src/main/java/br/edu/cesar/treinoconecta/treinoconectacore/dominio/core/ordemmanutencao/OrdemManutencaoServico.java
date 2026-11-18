package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import java.math.BigDecimal;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.ProblemaEquipamentoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.spi.IEquipamentoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.spi.IProblemaEquipamentoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.spi.IOrdemManutencaoRepositorio;

public final class OrdemManutencaoServico extends AgregadoServicoAbstrato<OrdemManutencaoIdentificador,
        OrdemManutencao, OperacaoEquipamentosAutorizacoes<OrdemManutencao>> {
    public static final String INEXISTENTE = "Registro %s não encontrado";

    private final IOrdemManutencaoRepositorio ordens;
    private final IProblemaEquipamentoRepositorio problemas;
    private final IEquipamentoRepositorio equipamentos;
    private final EquipamentoServico operacao;

    public OrdemManutencaoServico(ISegurancaServico seguranca, IOrdemManutencaoRepositorio ordens,
            IProblemaEquipamentoRepositorio problemas, IEquipamentoRepositorio equipamentos, EquipamentoServico operacao) {
        super(seguranca, ordens, "Ordem %s já existe", INEXISTENTE);
        this.ordens = Objects.requireNonNull(ordens);
        this.problemas = Objects.requireNonNull(problemas);
        this.equipamentos = Objects.requireNonNull(equipamentos);
        this.operacao = Objects.requireNonNull(operacao);
    }

    @Override
    public OperacaoEquipamentosAutorizacoes<OrdemManutencao> criarAutorizacoes(Optional<OrdemManutencaoIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean administrador = getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name());
        return new OperacaoEquipamentosAutorizacoes<>(administrador, false);
    }

    public OrdemManutencao abrir(ProblemaEquipamentoIdentificador identificador, DadosOrdem dados) {
        validarAutorizacao(Optional.empty(), OperacaoEquipamentosAutorizacoes::getOperar);
        bloquearEquipamento(identificador).validarAtivo();
        problemas.buscarPorIdentificador(identificador).validarAberto();
        Validate.validState(!ordens.existeAtiva(identificador), "O problema já tem ordem de manutenção ativa");
        return ordens.incluir(OrdemManutencao.abrir(identificador, dados, operacao.hoje(), getSegurancaServico().getUsuario()));
    }

    public void editar(OrdemManutencaoIdentificador identificador, DadosOrdem dados) {
        preparar(identificador);
        ordens.alterar(ordens.buscarPorIdentificador(identificador).editar(dados));
    }

    public void iniciar(OrdemManutencaoIdentificador identificador, LocalDate data) {
        var equipamento = preparar(identificador);
        var atual = ordens.buscarPorIdentificador(identificador);
        ordens.alterar(atual.iniciar(data, operacao.hoje()));
        if (equipamento.situacao() != SituacaoEquipamento.EM_MANUTENCAO) {
            operacao.registrarMudanca(equipamento, equipamento.mudar(equipamento.area(), SituacaoEquipamento.EM_MANUTENCAO),
                    operacao.hoje(), Optional.empty(), Optional.of("Início de ordem de manutenção"));
        }
    }

    public void concluir(OrdemManutencaoIdentificador identificador, LocalDate data, String servico,
            Optional<BigDecimal> custo) {
        preparar(identificador);
        var ordem = ordens.buscarPorIdentificador(identificador).concluir(data, servico, custo, operacao.hoje());
        var problema = problemas.buscarPorIdentificador(ordem.problema()).resolver(data, servico, operacao.hoje());
        ordens.alterar(ordem);
        problemas.alterar(problema);
    }

    public void cancelar(OrdemManutencaoIdentificador identificador, String justificativa) {
        preparar(identificador);
        ordens.alterar(ordens.buscarPorIdentificador(identificador).cancelar(operacao.hoje(), justificativa));
    }

    private Equipamento preparar(OrdemManutencaoIdentificador identificador) {
        validarAutorizacao(Optional.of(identificador), OperacaoEquipamentosAutorizacoes::getOperar);
        return bloquearEquipamento(ordens.identificarProblema(identificador));
    }

    private Equipamento bloquearEquipamento(ProblemaEquipamentoIdentificador problema) {
        return equipamentos.bloquear(problemas.identificarEquipamento(problema));
    }
}
