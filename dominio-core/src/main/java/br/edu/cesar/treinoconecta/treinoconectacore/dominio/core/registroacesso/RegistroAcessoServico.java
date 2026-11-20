package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.spi.IAdesaoPlanoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.spi.IMensalidadeRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.spi.IRegistroAcessoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

/** Controle de entrada e saída dos alunos (F006). */
public final class RegistroAcessoServico
        extends AgregadoServicoAbstrato<RegistroAcessoIdentificador, RegistroAcesso, RegistroAcessoAutorizacoes> {
    public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe um registro de acesso com o identificador %s";
    public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe registro de acesso com o identificador %s";
    public static final String ALUNO_INEXISTENTE_MODELO = "Não existe aluno com o identificador %s";
    public static final String ALUNO_INATIVO = "Entrada negada: o cadastro do aluno está inativo";
    public static final String ALUNO_BLOQUEADO_MODELO = "Entrada negada: o aluno está bloqueado (%s)";
    public static final String SEM_MATRICULA = "Entrada negada: o aluno não possui matrícula ativa";
    public static final String PLANO_VENCIDO = "Entrada negada: o plano está fora da validade, com mensalidade em atraso";
    public static final String FORA_DO_HORARIO_MODELO = "Entrada negada: o plano permite entradas das %s às %s";
    public static final String ENTRADA_DUPLICADA = "O aluno já está na academia. Registre a saída antes de uma nova entrada";
    public static final String CAPACIDADE_ATINGIDA_MODELO = "Entrada negada: a academia atingiu a capacidade máxima de %d alunos";
    public static final String SEM_ENTRADA_ABERTA = "Não há entrada aberta para este aluno";

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final IRegistroAcessoRepositorio repositorio;
    private final IAlunoRepositorio alunos;
    private final IAdesaoPlanoRepositorio adesoes;
    private final IMensalidadeRepositorio mensalidades;
    private final ParametrosAcesso parametros;
    private final Clock relogio;

    public RegistroAcessoServico(ISegurancaServico seguranca, IRegistroAcessoRepositorio repositorio,
            IAlunoRepositorio alunos, IAdesaoPlanoRepositorio adesoes, IMensalidadeRepositorio mensalidades,
            ParametrosAcesso parametros, Clock relogio) {
        super(seguranca, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.alunos = Objects.requireNonNull(alunos);
        this.adesoes = Objects.requireNonNull(adesoes);
        this.mensalidades = Objects.requireNonNull(mensalidades);
        this.parametros = Objects.requireNonNull(parametros);
        this.relogio = Objects.requireNonNull(relogio);
    }

    @Override
    public RegistroAcessoAutorizacoes criarAutorizacoes(Optional<RegistroAcessoIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean administrador = getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name());
        return new RegistroAcessoAutorizacoes(administrador, administrador);
    }

    /** Valida RN001 a RN005 e RN010 e, se a entrada for autorizada, registra-a (RN006). */
    public RegistroAcesso registrarEntrada(AlunoIdentificador identificadorAluno) {
        Objects.requireNonNull(identificadorAluno);
        validarAutorizacao(Optional.empty(), RegistroAcessoAutorizacoes::getRegistrar);
        validarExistencia(alunos::existePorIdentificador, identificadorAluno, ALUNO_INEXISTENTE_MODELO);
        var aluno = alunos.bloquear(identificadorAluno);
        var agora = Instant.now(relogio);
        var local = agora.atZone(ParametrosAcademia.FUSO);
        Validate.validState(aluno.isAtivo(), ALUNO_INATIVO);
        var bloqueio = aluno.getBloqueio();
        Validate.validState(bloqueio.isEmpty(), ALUNO_BLOQUEADO_MODELO,
                bloqueio.map(item -> item.motivo()).orElse(""));
        Validate.validState(adesoes.buscarAtivaDoAluno(identificadorAluno).isPresent(), SEM_MATRICULA);
        Validate.validState(!mensalidades.existeAtrasadaDoAluno(identificadorAluno, local.toLocalDate()), PLANO_VENCIDO);
        Validate.validState(parametros.dentroDoHorario(local.toLocalTime()), FORA_DO_HORARIO_MODELO,
                parametros.abertura().format(HORA), parametros.fechamento().format(HORA));
        Validate.validState(repositorio.bloquearAbertoDoAluno(identificadorAluno).isEmpty(), ENTRADA_DUPLICADA);
        long presentes = repositorio.contarPresentesComBloqueio(aluno.getAcademia());
        Validate.validState(presentes < parametros.capacidadeMaxima(), CAPACIDADE_ATINGIDA_MODELO,
                parametros.capacidadeMaxima());
        return repositorio.incluir(RegistroAcesso.entrar(identificadorAluno, agora));
    }

    /** RN007 a RN009: encerra a entrada aberta, calcula a permanência e marca para verificação se passar do limite. */
    public RegistroAcesso registrarSaida(AlunoIdentificador identificadorAluno) {
        Objects.requireNonNull(identificadorAluno);
        validarAutorizacao(Optional.empty(), RegistroAcessoAutorizacoes::getRegistrar);
        validarExistencia(alunos::existePorIdentificador, identificadorAluno, ALUNO_INEXISTENTE_MODELO);
        var aberto = repositorio.bloquearAbertoDoAluno(identificadorAluno)
                .orElseThrow(() -> new IllegalStateException(SEM_ENTRADA_ABERTA));
        var encerrado = aberto.sair(Instant.now(relogio), parametros.permanenciaMaxima());
        repositorio.alterar(encerrado);
        return encerrado;
    }

    /** Painel de presentes, histórico e frequência são consultas da recepção. */
    public void validarConsulta() {
        validarAutorizacao(Optional.empty(), RegistroAcessoAutorizacoes::getBuscar);
    }

    public ParametrosAcesso getParametros() { return parametros; }

    public LocalDate hoje() {
        return LocalDate.now(relogio.withZone(ParametrosAcademia.FUSO));
    }
}
