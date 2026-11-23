package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.spi.IAdesaoPlanoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.Mensalidade;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.spi.IMensalidadeRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.Plano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.spi.IPlanoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

public final class AdesaoPlanoServico
        extends AgregadoServicoAbstrato<AdesaoPlanoIdentificador, AdesaoPlano, AdesaoPlanoAutorizacoes> {
    public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe uma adesão com o identificador %s";
    public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe adesão com o identificador %s";
    public static final String ALUNO_INEXISTENTE_MODELO = "Não existe aluno com o identificador %s";
    public static final String PLANO_INEXISTENTE_MODELO = "Não existe plano com o identificador %s";
    public static final String PLANO_INDISPONIVEL = "Somente planos ativos podem receber novas adesões";
    public static final String ADESAO_ATIVA_EXISTENTE = "O aluno já possui um plano ativo. Troque ou cancele o plano atual antes de aderir a outro";
    public static final String ADESAO_NAO_ATIVA = "Somente uma adesão ativa pode ser trocada ou cancelada";
    public static final String MESMO_PLANO = "Escolha um plano diferente do plano atual";
    public static final String RECORRENCIA_INVALIDA = "A recorrência deve ter de " + AdesaoPlano.RECORRENCIA_MINIMA + " a "
            + AdesaoPlano.RECORRENCIA_MAXIMA + " cobranças";
    public static final String PRAZO_NAO_CUMPRIDO = "A adesão só é concluída depois do último período contratado";

    private final IAdesaoPlanoRepositorio repositorio;
    private final IPlanoRepositorio planos;
    private final IMensalidadeRepositorio mensalidades;
    private final IAlunoRepositorio alunos;
    private final Clock relogio;

    public AdesaoPlanoServico(ISegurancaServico seguranca, IAdesaoPlanoRepositorio repositorio, IPlanoRepositorio planos,
            IMensalidadeRepositorio mensalidades, IAlunoRepositorio alunos, Clock relogio) {
        super(seguranca, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.planos = Objects.requireNonNull(planos);
        this.mensalidades = Objects.requireNonNull(mensalidades);
        this.alunos = Objects.requireNonNull(alunos);
        this.relogio = Objects.requireNonNull(relogio);
    }

    @Override
    public AdesaoPlanoAutorizacoes criarAutorizacoes(Optional<AdesaoPlanoIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean administrador = getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name());
        boolean aluno = getSegurancaServico().possuiAutorizacao(Autorizacao.USUARIO.name());
        boolean buscar = administrador || aluno;
        if (aluno && identificador.isPresent()) {
            var autenticado = alunos.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow();
            buscar = repositorio.buscarPorIdentificador(identificador.get()).getAluno().equals(autenticado);
        }
        return new AdesaoPlanoAutorizacoes(buscar, administrador, administrador, administrador, administrador);
    }

    public AdesaoPlano aderir(AlunoIdentificador aluno, PlanoIdentificador plano) {
        return aderir(aluno, plano, Optional.empty());
    }

    /**
     * A recorrência opcional define quantas cobranças a adesão terá. Uma adesão anterior cuja recorrência já terminou é
     * concluída antes, liberando o aluno para o novo plano.
     */
    public AdesaoPlano aderir(AlunoIdentificador aluno, PlanoIdentificador plano, Optional<Integer> quantidadePeriodos) {
        Objects.requireNonNull(aluno);
        Objects.requireNonNull(plano);
        Objects.requireNonNull(quantidadePeriodos);
        validarAutorizacao(Optional.empty(), AdesaoPlanoAutorizacoes::getAderir);
        validarRecorrencia(quantidadePeriodos);
        validarExistencia(alunos::existePorIdentificador, aluno, ALUNO_INEXISTENTE_MODELO);
        validarExistencia(planos::existePorIdentificador, plano, PLANO_INEXISTENTE_MODELO);
        bloquearAluno(aluno);
        repositorio.buscarAtivaDoAluno(aluno).filter(atual -> atual.prazoCumpridoEm(hoje()))
                .ifPresent(atual -> concluirComPendentes(repositorio.bloquear(atual.getIdentificador().orElseThrow())));
        Validate.validState(repositorio.buscarAtivaDoAluno(aluno).isEmpty(), ADESAO_ATIVA_EXISTENTE);
        return incluirComPrimeiraMensalidade(aluno, planos.bloquear(plano), quantidadePeriodos);
    }

    public AdesaoPlano trocar(AdesaoPlanoIdentificador identificador, PlanoIdentificador novoPlano) {
        return trocar(identificador, novoPlano, Optional.empty());
    }

    public AdesaoPlano trocar(AdesaoPlanoIdentificador identificador, PlanoIdentificador novoPlano,
            Optional<Integer> quantidadePeriodos) {
        Objects.requireNonNull(identificador);
        Objects.requireNonNull(novoPlano);
        Objects.requireNonNull(quantidadePeriodos);
        validarRecorrencia(quantidadePeriodos);
        validarAutorizacao(Optional.of(identificador), AdesaoPlanoAutorizacoes::getTrocar);
        validarExistencia(identificador);
        validarExistencia(planos::existePorIdentificador, novoPlano, PLANO_INEXISTENTE_MODELO);
        bloquearAluno(repositorio.buscarPorIdentificador(identificador).getAluno());
        var atual = repositorio.bloquear(identificador);
        Validate.validState(atual.ativa(), ADESAO_NAO_ATIVA);
        Validate.validState(!atual.getPlano().equals(novoPlano), MESMO_PLANO);
        var plano = planos.bloquear(novoPlano);
        Validate.validState(plano.ativo(), PLANO_INDISPONIVEL);
        repositorio.alterar(atual.encerrarPorTroca(hoje()));
        return incluirComPrimeiraMensalidade(atual.getAluno(), plano, quantidadePeriodos);
    }

    public AdesaoPlano cancelar(AdesaoPlanoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), AdesaoPlanoAutorizacoes::getCancelar);
        validarExistencia(identificador);
        var cancelada = repositorio.bloquear(identificador).cancelar(hoje());
        repositorio.alterar(cancelada);
        return cancelada;
    }

    /**
     * Gera as mensalidades dos períodos já iniciados de todas as adesões ativas, sem duplicar períodos, e conclui as
     * adesões cuja recorrência terminou.
     */
    public int gerarMensalidades() {
        validarAutorizacao(Optional.empty(), AdesaoPlanoAutorizacoes::getGerarMensalidades);
        int geradas = 0;
        for (var adesao : repositorio.buscarAtivas()) {
            var bloqueada = repositorio.bloquear(adesao.getIdentificador().orElseThrow());
            geradas += gerarPendentes(bloqueada);
            if (bloqueada.prazoCumpridoEm(hoje())) {
                repositorio.alterar(bloqueada.concluir(hoje()));
            }
        }
        return geradas;
    }

    /**
     * Rotina do sistema, sem usuário autenticado: conclui as adesões ativas cuja recorrência terminou, gerando antes as
     * mensalidades que faltarem. Adesões sem recorrência não são alteradas.
     */
    public int concluirPrazosCumpridos() {
        int concluidas = 0;
        for (var adesao : repositorio.buscarAtivas()) {
            if (adesao.prazoCumpridoEm(hoje())) {
                concluirComPendentes(repositorio.bloquear(adesao.getIdentificador().orElseThrow()));
                concluidas++;
            }
        }
        return concluidas;
    }

    private void concluirComPendentes(AdesaoPlano adesao) {
        gerarPendentes(adesao);
        repositorio.alterar(adesao.concluir(hoje()));
    }

    private void validarRecorrencia(Optional<Integer> quantidadePeriodos) {
        quantidadePeriodos.ifPresent(quantidade -> Validate.isTrue(
                quantidade >= AdesaoPlano.RECORRENCIA_MINIMA && quantidade <= AdesaoPlano.RECORRENCIA_MAXIMA,
                RECORRENCIA_INVALIDA));
    }

    private AdesaoPlano incluirComPrimeiraMensalidade(AlunoIdentificador aluno, Plano plano,
            Optional<Integer> quantidadePeriodos) {
        var nova = repositorio.incluir(AdesaoPlano.aderir(aluno, plano, hoje(), quantidadePeriodos));
        gerarPendentes(nova);
        return nova;
    }

    private int gerarPendentes(AdesaoPlano adesao) {
        var identificador = adesao.getIdentificador().orElseThrow();
        var valor = planos.buscarPorIdentificador(adesao.getPlano()).getValor();
        int geradas = 0;
        for (var periodo : adesao.periodosIniciadosAte(hoje())) {
            if (!mensalidades.existePorAdesaoEPeriodo(identificador, periodo.inicio())) {
                mensalidades.incluir(Mensalidade.gerar(adesao, periodo, valor));
                geradas++;
            }
        }
        return geradas;
    }

    private void bloquearAluno(AlunoIdentificador aluno) {
        alunos.bloquearPorUsuario(alunos.buscarPorIdentificador(aluno).getUsuario());
    }

    private LocalDate hoje() {
        return relogio.instant().atZone(ParametrosAcademia.FUSO).toLocalDate();
    }
}
