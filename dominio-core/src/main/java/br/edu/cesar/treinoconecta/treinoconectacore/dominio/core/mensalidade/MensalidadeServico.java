package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.spi.IMensalidadeRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

public final class MensalidadeServico
        extends AgregadoServicoAbstrato<MensalidadeIdentificador, Mensalidade, MensalidadeAutorizacoes> {
    public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe uma mensalidade com o identificador %s";
    public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe mensalidade com o identificador %s";
    public static final String MENSALIDADE_NAO_ABERTA = "Somente mensalidades em aberto ou em atraso podem receber pagamento";
    public static final String PAGAMENTO_INEXISTENTE = "O pagamento informado não pertence a esta mensalidade";
    public static final String PAGAMENTO_JA_ESTORNADO = "Este pagamento já foi estornado";
    public static final String MOTIVO_OBRIGATORIO = "Informe o motivo do estorno com até "
            + ParametrosFinanceiros.TAMANHO_MOTIVO_ESTORNO + " caracteres";

    private final IMensalidadeRepositorio repositorio;
    private final IAlunoRepositorio alunos;
    private final Clock relogio;

    public MensalidadeServico(ISegurancaServico seguranca, IMensalidadeRepositorio repositorio, IAlunoRepositorio alunos,
            Clock relogio) {
        super(seguranca, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.alunos = Objects.requireNonNull(alunos);
        this.relogio = Objects.requireNonNull(relogio);
    }

    @Override
    public MensalidadeAutorizacoes criarAutorizacoes(Optional<MensalidadeIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean administrador = getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name());
        boolean aluno = getSegurancaServico().possuiAutorizacao(Autorizacao.USUARIO.name());
        boolean buscar = administrador || aluno;
        if (aluno && identificador.isPresent()) {
            var autenticado = alunos.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow();
            buscar = repositorio.buscarPorIdentificador(identificador.get()).getAluno().equals(autenticado);
        }
        return new MensalidadeAutorizacoes(buscar, administrador, administrador);
    }

    public Mensalidade registrarPagamento(MensalidadeIdentificador identificador, FormaPagamento forma) {
        Objects.requireNonNull(identificador);
        Objects.requireNonNull(forma);
        validarAutorizacao(Optional.of(identificador), MensalidadeAutorizacoes::getRegistrarPagamento);
        validarExistencia(identificador);
        var paga = repositorio.bloquear(identificador).registrarPagamento(forma, relogio.instant(), hoje());
        repositorio.alterar(paga);
        return paga;
    }

    public Mensalidade estornarPagamento(MensalidadeIdentificador identificador, long pagamento, String motivo) {
        Objects.requireNonNull(identificador);
        Objects.requireNonNull(motivo);
        validarAutorizacao(Optional.of(identificador), MensalidadeAutorizacoes::getEstornarPagamento);
        validarExistencia(identificador);
        var estornada = repositorio.bloquear(identificador).estornarPagamento(pagamento, motivo, relogio.instant());
        repositorio.alterar(estornada);
        return estornada;
    }

    public LocalDate hoje() {
        return relogio.instant().atZone(ParametrosAcademia.FUSO).toLocalDate();
    }
}
