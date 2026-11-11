package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

public final class Meta implements IAgregado<MetaIdentificador> {
    private static final BigDecimal CEM = new BigDecimal("100");

    private final Optional<MetaIdentificador> identificador;
    private final AlunoIdentificador aluno;
    private final PersonalIdentificador personal;
    private final IndicadorFisico indicador;
    private final BigDecimal valorInicial;
    private final BigDecimal valorDesejado;
    private final LocalDate inicio;
    private final LocalDate fim;
    private final AvaliacaoFisicaIdentificador avaliacaoReferencia;
    private final SituacaoMeta situacao;
    private final Optional<LocalDate> dataAtingida;

    public Meta(Optional<MetaIdentificador> identificador, AlunoIdentificador aluno, PersonalIdentificador personal,
            IndicadorFisico indicador, BigDecimal valorInicial, BigDecimal valorDesejado, LocalDate inicio, LocalDate fim,
            AvaliacaoFisicaIdentificador avaliacaoReferencia, SituacaoMeta situacao, Optional<LocalDate> dataAtingida) {
        this.identificador = Objects.requireNonNull(identificador);
        this.aluno = Objects.requireNonNull(aluno);
        this.personal = Objects.requireNonNull(personal);
        this.indicador = Objects.requireNonNull(indicador);
        Objects.requireNonNull(valorInicial);
        Validate.notNull(valorDesejado, MetaServico.VALOR_DESEJADO_INVALIDO);
        this.inicio = Validate.notNull(inicio, MetaServico.PERIODO_OBRIGATORIO);
        this.fim = Validate.notNull(fim, MetaServico.PERIODO_OBRIGATORIO);
        Validate.isTrue(valorDesejado.signum() > 0, MetaServico.VALOR_DESEJADO_INVALIDO);
        Validate.isTrue(valorDesejado.compareTo(valorInicial) != 0, MetaServico.VALOR_DESEJADO_IGUAL_AO_ATUAL);
        Validate.isTrue(!inicio.isAfter(fim), MetaServico.PERIODO_INVERTIDO);
        this.valorInicial = valorInicial.setScale(2, RoundingMode.HALF_UP);
        this.valorDesejado = valorDesejado.setScale(2, RoundingMode.HALF_UP);
        this.avaliacaoReferencia = Objects.requireNonNull(avaliacaoReferencia);
        this.situacao = Objects.requireNonNull(situacao);
        this.dataAtingida = Objects.requireNonNull(dataAtingida);
    }

    public static Meta criar(AlunoIdentificador aluno, PersonalIdentificador personal, IndicadorFisico indicador,
            BigDecimal valorInicial, BigDecimal valorDesejado, LocalDate inicio, LocalDate fim,
            AvaliacaoFisicaIdentificador avaliacaoReferencia, LocalDate hoje) {
        var meta = new Meta(Optional.empty(), aluno, personal, indicador, valorInicial, valorDesejado, inicio, fim,
                avaliacaoReferencia, SituacaoMeta.ATIVA, Optional.empty());
        validarFimDoPeriodo(fim, hoje);
        return meta;
    }

    /** Valida os dados informados antes de consultar a avaliação de referência. */
    public static void validarDados(BigDecimal valorDesejado, LocalDate inicio, LocalDate fim, LocalDate hoje) {
        Validate.notNull(valorDesejado, MetaServico.VALOR_DESEJADO_INVALIDO);
        Validate.notNull(inicio, MetaServico.PERIODO_OBRIGATORIO);
        Validate.notNull(fim, MetaServico.PERIODO_OBRIGATORIO);
        Objects.requireNonNull(hoje);
        Validate.isTrue(valorDesejado.signum() > 0, MetaServico.VALOR_DESEJADO_INVALIDO);
        Validate.isTrue(!inicio.isAfter(fim), MetaServico.PERIODO_INVERTIDO);
        validarFimDoPeriodo(fim, hoje);
    }

    private static void validarFimDoPeriodo(LocalDate fim, LocalDate hoje) {
        Validate.isTrue(!fim.isBefore(hoje), MetaServico.PERIODO_ENCERRADO);
    }

    public Meta ajustar(BigDecimal novoValorDesejado, LocalDate novoInicio, LocalDate novoFim, LocalDate hoje) {
        var ajustada = new Meta(identificador, aluno, personal, indicador, valorInicial, novoValorDesejado, novoInicio,
                novoFim, avaliacaoReferencia, situacao, dataAtingida);
        validarFimDoPeriodo(novoFim, hoje);
        validarAtiva();
        return ajustada;
    }

    public Meta encerrar() {
        validarAtiva();
        return comSituacao(SituacaoMeta.ENCERRADA, Optional.empty());
    }

    public Meta substituir() {
        validarAtiva();
        return comSituacao(SituacaoMeta.SUBSTITUIDA, Optional.empty());
    }

    public Meta marcarAtingida(LocalDate data) {
        Objects.requireNonNull(data);
        validarAtiva();
        return comSituacao(SituacaoMeta.ATINGIDA, Optional.of(data));
    }

    public boolean atingidaPor(BigDecimal valor, LocalDate data) {
        Objects.requireNonNull(valor);
        Objects.requireNonNull(data);
        if (situacao != SituacaoMeta.ATIVA || data.isBefore(inicio) || data.isAfter(fim)) {
            return false;
        }
        boolean reduzir = valorDesejado.compareTo(valorInicial) < 0;
        return reduzir ? valor.compareTo(valorDesejado) <= 0 : valor.compareTo(valorDesejado) >= 0;
    }

    public BigDecimal percentualCumprimento(BigDecimal valorAtual) {
        Objects.requireNonNull(valorAtual);
        if (situacao == SituacaoMeta.ATINGIDA) {
            return CEM;
        }
        var percorrido = valorAtual.subtract(valorInicial);
        var distancia = valorDesejado.subtract(valorInicial);
        var percentual = percorrido.multiply(CEM).divide(distancia, 2, RoundingMode.HALF_UP);
        return percentual.max(BigDecimal.ZERO).min(CEM);
    }

    public StatusCumprimento statusCumprimento(BigDecimal valorAtual) {
        return StatusCumprimento.classificar(percentualCumprimento(valorAtual));
    }

    private void validarAtiva() {
        Validate.validState(situacao == SituacaoMeta.ATIVA, MetaServico.META_NAO_ATIVA);
    }

    private Meta comSituacao(SituacaoMeta novaSituacao, Optional<LocalDate> novaData) {
        return new Meta(identificador, aluno, personal, indicador, valorInicial, valorDesejado, inicio, fim,
                avaliacaoReferencia, novaSituacao, novaData);
    }

    public Optional<MetaIdentificador> getIdentificador() { return identificador; }
    public AlunoIdentificador getAluno() { return aluno; }
    public PersonalIdentificador getPersonal() { return personal; }
    public IndicadorFisico getIndicador() { return indicador; }
    public BigDecimal getValorInicial() { return valorInicial; }
    public BigDecimal getValorDesejado() { return valorDesejado; }
    public LocalDate getInicio() { return inicio; }
    public LocalDate getFim() { return fim; }
    public AvaliacaoFisicaIdentificador getAvaliacaoReferencia() { return avaliacaoReferencia; }
    public SituacaoMeta getSituacao() { return situacao; }
    public Optional<LocalDate> getDataAtingida() { return dataAtingida; }
}
