package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.spi.IAvaliacaoFisicaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.spi.IMetaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.spi.IPersonalRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;

public final class AvaliacaoFisicaServico
        extends AgregadoServicoAbstrato<AvaliacaoFisicaIdentificador, AvaliacaoFisica, AvaliacaoFisicaAutorizacoes> {
    public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe uma avaliação física com o identificador %s";
    public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe avaliação física com o identificador %s";
    public static final String ALUNO_INEXISTENTE_MODELO = "Não existe aluno com o identificador %s";
    public static final String DATA_OBRIGATORIA = "Informe a data da avaliação";
    public static final String PESO_OBRIGATORIO = "Informe o peso";
    public static final String ALTURA_OBRIGATORIA = "Informe a altura";
    public static final String DATA_FUTURA = "A data da avaliação não pode ser futura";
    public static final String DATA_FORA_DE_ORDEM_MODELO = "A data da avaliação deve ser posterior à última avaliação, de %s";
    public static final String DATA_CORRIGIDA_FORA_DE_ORDEM = "A data corrigida deve manter a ordem cronológica das avaliações";
    public static final String PESO_FORA_DOS_LIMITES_MODELO = "O peso deve estar entre %s e %s kg";
    public static final String ALTURA_FORA_DOS_LIMITES_MODELO = "A altura deve estar entre %s e %s cm";
    public static final String MEDIDA_FORA_DOS_LIMITES_MODELO = "A medida %s deve estar entre %s e %s cm";
    public static final String MEDIDA_REPETIDA_MODELO = "A medida %s foi informada mais de uma vez";
    public static final String INDICADOR_NAO_E_MEDIDA_MODELO = "%s não é uma medida corporal";
    public static final String VERSAO_INVALIDA = "A versão da avaliação deve ser maior que zero";
    public static final String AVALIACAO_NAO_VALIDA = "Esta avaliação já foi corrigida ou excluída";
    public static final String REFERENCIA_DE_COMPARACAO = "Esta avaliação é referência de uma avaliação posterior e não pode ser excluída";
    public static final String REFERENCIA_DE_META = "Esta avaliação é referência de uma meta e não pode ser excluída";
    public static final String REFERENCIA_INVALIDA = "A referência deve ser uma avaliação válida, anterior e do mesmo aluno";
    public static final String ALUNO_OBRIGATORIO = "Informe o aluno";
    public static final String DADOS_DE_OUTRO_ALUNO = "O aluno só consulta os próprios dados";

    private final IAvaliacaoFisicaRepositorio repositorio;
    private final IAlunoRepositorio alunos;
    private final IPersonalRepositorio personais;
    private final IMetaRepositorio metas;
    private final Clock relogio;

    public AvaliacaoFisicaServico(ISegurancaServico seguranca, IAvaliacaoFisicaRepositorio repositorio,
            IAlunoRepositorio alunos, IPersonalRepositorio personais, IMetaRepositorio metas, Clock relogio) {
        super(seguranca, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.alunos = Objects.requireNonNull(alunos);
        this.personais = Objects.requireNonNull(personais);
        this.metas = Objects.requireNonNull(metas);
        this.relogio = Objects.requireNonNull(relogio);
    }

    @Override
    public AvaliacaoFisicaAutorizacoes criarAutorizacoes(Optional<AvaliacaoFisicaIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean personal = possuiPapel(Autorizacao.PERSONAL);
        boolean aluno = possuiPapel(Autorizacao.USUARIO);
        boolean buscar = personal || aluno;
        if (aluno && identificador.isPresent()) {
            buscar = repositorio.buscarPorIdentificador(identificador.get()).getAluno().equals(alunoAutenticado());
        }
        return new AvaliacaoFisicaAutorizacoes(buscar, personal, personal, personal);
    }

    public AvaliacaoFisica registrar(AlunoIdentificador aluno, LocalDate data, BigDecimal peso, BigDecimal altura,
            List<MedidaCorporal> medidas) {
        Objects.requireNonNull(aluno);
        validarAutorizacao(Optional.empty(), AvaliacaoFisicaAutorizacoes::getRegistrar);
        var agora = relogio.instant();
        var nova = AvaliacaoFisica.registrar(aluno, personalAutenticado(), data, peso, altura, medidas, hoje(agora), agora);
        validarExistencia(alunos::existePorIdentificador, aluno, ALUNO_INEXISTENTE_MODELO);
        bloquearAluno(aluno);
        historicoDoAluno(aluno).validarNovaData(data);
        var salva = repositorio.incluir(nova);
        marcarMetasAtingidas(salva);
        return salva;
    }

    public AvaliacaoFisica corrigir(AvaliacaoFisicaIdentificador identificador, LocalDate data, BigDecimal peso,
            BigDecimal altura, List<MedidaCorporal> medidas) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), AvaliacaoFisicaAutorizacoes::getCorrigir);
        validarExistencia(identificador);
        bloquearAluno(repositorio.buscarPorIdentificador(identificador).getAluno());
        var original = repositorio.bloquear(identificador);
        var agora = relogio.instant();
        var corrigida = original.corrigir(personalAutenticado(), data, peso, altura, medidas, hoje(agora), agora);
        historicoDoAluno(original.getAluno()).validarDataCorrigida(original, data);
        repositorio.alterar(original.substituir());
        var salva = repositorio.incluir(corrigida);
        metas.transferirReferencia(identificador, salva.getIdentificador().orElseThrow());
        marcarMetasAtingidas(salva);
        return salva;
    }

    public void remover(AvaliacaoFisicaIdentificador identificador) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), AvaliacaoFisicaAutorizacoes::getRemover);
        validarExistencia(identificador);
        bloquearAluno(repositorio.buscarPorIdentificador(identificador).getAluno());
        var avaliacao = repositorio.bloquear(identificador);
        var excluida = avaliacao.excluir();
        Validate.validState(historicoDoAluno(avaliacao.getAluno()).isMaisRecente(avaliacao), REFERENCIA_DE_COMPARACAO);
        Validate.validState(!metas.existePorAvaliacaoReferencia(identificador), REFERENCIA_DE_META);
        repositorio.alterar(excluida);
    }

    public HistoricoDeAvaliacao consultarHistorico(AlunoIdentificador aluno) {
        Objects.requireNonNull(aluno);
        validarAcessoAoAluno(aluno);
        return historicoDoAluno(aluno);
    }

    /** Sem referência informada, usa a avaliação imediatamente anterior. */
    public Optional<AvaliacaoFisica> buscarReferencia(AvaliacaoFisica atual,
            Optional<AvaliacaoFisicaIdentificador> informada) {
        Objects.requireNonNull(atual);
        Objects.requireNonNull(informada);
        var historico = consultarHistorico(atual.getAluno());
        if (informada.isEmpty()) {
            return historico.anteriorA(atual);
        }
        var referencia = repositorio.buscarPorIdentificador(informada.get());
        Validate.isTrue(referencia.isValida() && referencia.getAluno().equals(atual.getAluno())
                && referencia.getData().isBefore(atual.getData()), REFERENCIA_INVALIDA);
        return Optional.of(referencia);
    }

    /** O personal escolhe o aluno. O aluno só pode consultar a si mesmo. */
    public AlunoIdentificador resolverAluno(Optional<AlunoIdentificador> informado) {
        Objects.requireNonNull(informado);
        if (possuiPapel(Autorizacao.PERSONAL)) {
            Validate.isTrue(informado.isPresent(), ALUNO_OBRIGATORIO);
            return informado.get();
        }
        var aluno = alunoAutenticado();
        if (informado.isPresent() && !informado.get().equals(aluno)) {
            throw new NaoAutorizadoExcecao(DADOS_DE_OUTRO_ALUNO);
        }
        return aluno;
    }

    public boolean podeGerenciar() {
        return possuiPapel(Autorizacao.PERSONAL);
    }

    private void marcarMetasAtingidas(AvaliacaoFisica avaliacao) {
        for (var meta : metas.buscarAtivasPorAluno(avaliacao.getAluno())) {
            var valor = avaliacao.valorDe(meta.getIndicador());
            if (valor.isPresent() && meta.atingidaPor(valor.get(), avaliacao.getData())) {
                metas.alterar(meta.marcarAtingida(avaliacao.getData()));
            }
        }
    }

    private HistoricoDeAvaliacao historicoDoAluno(AlunoIdentificador aluno) {
        return new HistoricoDeAvaliacao(repositorio.buscarValidasPorAluno(aluno));
    }

    private void bloquearAluno(AlunoIdentificador aluno) {
        alunos.bloquearPorUsuario(alunos.buscarPorIdentificador(aluno).getUsuario());
    }

    private void validarAcessoAoAluno(AlunoIdentificador aluno) {
        if (!possuiPapel(Autorizacao.PERSONAL) && !aluno.equals(alunoAutenticado())) {
            throw new NaoAutorizadoExcecao(DADOS_DE_OUTRO_ALUNO);
        }
    }

    private static LocalDate hoje(Instant agora) {
        return agora.atZone(ParametrosAcademia.FUSO).toLocalDate();
    }

    private boolean possuiPapel(Autorizacao papel) {
        return getSegurancaServico().possuiAutorizacao(papel.name());
    }

    private PersonalIdentificador personalAutenticado() {
        return personais.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow();
    }

    private AlunoIdentificador alunoAutenticado() {
        return alunos.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow();
    }
}
