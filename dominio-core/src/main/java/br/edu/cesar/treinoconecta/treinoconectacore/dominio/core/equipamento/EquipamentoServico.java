package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.RegistroEquipamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.ResultadoInspecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.spi.IRegistroEquipamentoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.spi.IProblemaEquipamentoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.spi.IEquipamentoRepositorio;

public final class EquipamentoServico extends AgregadoServicoAbstrato<EquipamentoIdentificador,
        Equipamento, OperacaoEquipamentosAutorizacoes<Equipamento>> {
    public static final String INEXISTENTE = "Registro %s não encontrado";

    private final IEquipamentoRepositorio equipamentos;
    private final IRegistroEquipamentoRepositorio registros;
    private final IProblemaEquipamentoRepositorio problemas;
    private final Clock relogio;

    public EquipamentoServico(ISegurancaServico seguranca, IEquipamentoRepositorio equipamentos,
            IRegistroEquipamentoRepositorio registros, IProblemaEquipamentoRepositorio problemas, Clock relogio) {
        super(seguranca, equipamentos, "Equipamento %s já cadastrado", INEXISTENTE);
        this.equipamentos = Objects.requireNonNull(equipamentos);
        this.registros = Objects.requireNonNull(registros);
        this.problemas = Objects.requireNonNull(problemas);
        this.relogio = Objects.requireNonNull(relogio);
    }

    @Override
    public OperacaoEquipamentosAutorizacoes<Equipamento> criarAutorizacoes(Optional<EquipamentoIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean administrador = getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name());
        return new OperacaoEquipamentosAutorizacoes<>(administrador, true);
    }

    public Equipamento cadastrar(DadosEquipamento dados, AreaEquipamento area) {
        validarAutorizacao(Optional.empty(), OperacaoEquipamentosAutorizacoes::getOperar);
        validarDados(dados, Optional.empty());
        return equipamentos.incluir(Equipamento.cadastrar(dados, area));
    }

    public void editar(EquipamentoIdentificador identificador, DadosEquipamento dados) {
        validarAutorizacao(Optional.of(identificador), OperacaoEquipamentosAutorizacoes::getOperar);
        var equipamento = equipamentos.bloquear(identificador);
        validarDados(dados, Optional.of(identificador));
        Validate.validState(equipamento.dados().aquisicao().equals(dados.aquisicao()) || !temHistorico(identificador),
                "A aquisição não pode ser alterada depois de registrar histórico");
        equipamentos.alterar(equipamento.editar(dados));
    }

    public void inativar(EquipamentoIdentificador identificador) {
        validarAutorizacao(Optional.of(identificador), OperacaoEquipamentosAutorizacoes::getOperar);
        var atual = equipamentos.bloquear(identificador);
        Validate.validState(!problemas.existeAberto(identificador), "Encerre os problemas em aberto antes de inativar");
        registrarMudanca(atual, atual.mudar(atual.area(), SituacaoEquipamento.INATIVO), hoje(),
                Optional.empty(), Optional.of("Equipamento inativado"));
    }

    @Override
    protected void validarExclusao(EquipamentoIdentificador identificador) {
        equipamentos.bloquear(identificador);
        Validate.validState(!temHistorico(identificador), "O equipamento possui histórico. Inative o cadastro.");
    }

    public boolean temHistorico(EquipamentoIdentificador identificador) {
        return registros.existePorEquipamento(identificador) || problemas.existePorEquipamento(identificador);
    }

    public LocalDate hoje() {
        return LocalDate.now(relogio.withZone(Equipamento.FUSO));
    }

    /** Usado por operações de domínio que já bloquearam o equipamento na mesma transação. */
    public void registrarMudanca(Equipamento anterior, Equipamento novo, LocalDate data,
            Optional<ResultadoInspecao> resultado, Optional<String> observacao) {
        var registro = new RegistroEquipamento(Optional.empty(), anterior.identificador().orElseThrow(),
                data, getSegurancaServico().getUsuario(), anterior.area(), novo.area(), anterior.situacao(),
                novo.situacao(), resultado, observacao);
        equipamentos.alterar(novo);
        registros.incluir(registro);
    }

    private void validarDados(DadosEquipamento dados, Optional<EquipamentoIdentificador> ignorado) {
        Validate.isTrue(!dados.aquisicao().isAfter(hoje()), "A aquisição não pode estar no futuro");
        validarInexistencia(valor -> equipamentos.existePatrimonio(valor, ignorado), dados.patrimonio(),
                "Já existe equipamento com patrimônio %s");
        dados.serie().ifPresent(serie -> validarInexistencia(valor -> equipamentos.existeSerie(valor, ignorado),
                serie, "Já existe equipamento com série %s"));
    }
}
