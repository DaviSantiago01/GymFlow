package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.CumprimentoMeta;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.IndicadorAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.PeriodoAnalise;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.SentidoIndicador;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

/** Meta de desempenho definida pelo gestor para um indicador da academia em um período. */
public final class MetaAcademia implements IAgregado<MetaAcademiaIdentificador> {
    public static final int TAMANHO_RESPONSAVEL = 100;
    private static final BigDecimal CEM = new BigDecimal("100");

    private final Optional<MetaAcademiaIdentificador> identificador;
    private final IndicadorAcademia indicador;
    private final BigDecimal valorDesejado;
    private final LocalDate inicio;
    private final LocalDate fim;
    private final SituacaoMetaAcademia situacao;
    private final Instant criacao;
    private final String responsavel;

    public MetaAcademia(Optional<MetaAcademiaIdentificador> identificador, IndicadorAcademia indicador,
            BigDecimal valorDesejado, LocalDate inicio, LocalDate fim, SituacaoMetaAcademia situacao, Instant criacao,
            String responsavel) {
        this.identificador = Objects.requireNonNull(identificador);
        this.indicador = Validate.notNull(indicador, MetaAcademiaServico.INDICADOR_OBRIGATORIO);
        Validate.notNull(valorDesejado, MetaAcademiaServico.VALOR_INVALIDO);
        Validate.isTrue(valorDesejado.signum() >= 0, MetaAcademiaServico.VALOR_INVALIDO);
        if (indicador.getUnidade() == IndicadorAcademia.Unidade.PERCENTUAL) {
            Validate.isTrue(valorDesejado.compareTo(CEM) <= 0, MetaAcademiaServico.PERCENTUAL_INVALIDO);
        }
        this.valorDesejado = valorDesejado.setScale(2, RoundingMode.HALF_UP);
        this.inicio = Validate.notNull(inicio, MetaAcademiaServico.PERIODO_OBRIGATORIO);
        this.fim = Validate.notNull(fim, MetaAcademiaServico.PERIODO_OBRIGATORIO);
        Validate.isTrue(!inicio.isAfter(fim), MetaAcademiaServico.PERIODO_INVERTIDO);
        this.situacao = Objects.requireNonNull(situacao);
        this.criacao = Objects.requireNonNull(criacao);
        this.responsavel = Validate.notBlank(responsavel, "Responsável obrigatório");
        Validate.isTrue(responsavel.length() <= TAMANHO_RESPONSAVEL, "Responsável muito longo");
    }

    public static MetaAcademia cadastrar(IndicadorAcademia indicador, BigDecimal valorDesejado, LocalDate inicio,
            LocalDate fim, String responsavel, Instant agora, LocalDate hoje) {
        var meta = new MetaAcademia(Optional.empty(), indicador, valorDesejado, inicio, fim, SituacaoMetaAcademia.ATIVA,
                agora, responsavel);
        Validate.isTrue(!fim.isBefore(hoje), MetaAcademiaServico.PERIODO_ENCERRADO);
        return meta;
    }

    public MetaAcademia encerrar() {
        Validate.validState(ativa(), MetaAcademiaServico.META_NAO_ATIVA);
        return comSituacao(SituacaoMetaAcademia.ENCERRADA);
    }

    public MetaAcademia substituir() {
        Validate.validState(ativa(), MetaAcademiaServico.META_NAO_ATIVA);
        return comSituacao(SituacaoMetaAcademia.SUBSTITUIDA);
    }

    private MetaAcademia comSituacao(SituacaoMetaAcademia nova) {
        return new MetaAcademia(identificador, indicador, valorDesejado, inicio, fim, nova, criacao, responsavel);
    }

    public boolean ativa() {
        return situacao == SituacaoMetaAcademia.ATIVA;
    }

    public boolean sobrepoe(LocalDate outroInicio, LocalDate outroFim) {
        return !inicio.isAfter(outroFim) && !fim.isBefore(outroInicio);
    }

    public PeriodoAnalise periodo() {
        return new PeriodoAnalise(inicio, fim);
    }

    /** Período já transcorrido da meta, até o dia informado, se a meta já começou. */
    public Optional<PeriodoAnalise> periodoApuradoAte(LocalDate hoje) {
        if (inicio.isAfter(hoje)) {
            return Optional.empty();
        }
        return Optional.of(new PeriodoAnalise(inicio, fim.isAfter(hoje) ? hoje : fim));
    }

    /**
     * RN016: a meta é atingida quando o realizado está no sentido desejado do indicador. O percentual de cumprimento
     * compara o realizado com o desejado, ou o inverso quando um valor menor é melhor.
     */
    public CumprimentoMeta cumprimento(BigDecimal realizado) {
        Objects.requireNonNull(realizado);
        boolean atingida = indicador.getSentido().atinge(realizado, valorDesejado);
        BigDecimal percentual;
        if (indicador.getSentido() == SentidoIndicador.MAIOR_MELHOR) {
            percentual = valorDesejado.signum() == 0 ? CEM.setScale(1)
                    : realizado.multiply(CEM).divide(valorDesejado, 1, RoundingMode.HALF_UP);
        } else {
            percentual = realizado.signum() == 0 ? CEM.setScale(1)
                    : valorDesejado.multiply(CEM).divide(realizado, 1, RoundingMode.HALF_UP);
        }
        return new CumprimentoMeta(identificador.map(Object::toString).orElse(""), indicador, valorDesejado, realizado,
                percentual, atingida, inicio, fim);
    }

    public Optional<MetaAcademiaIdentificador> getIdentificador() { return identificador; }
    public IndicadorAcademia getIndicador() { return indicador; }
    public BigDecimal getValorDesejado() { return valorDesejado; }
    public LocalDate getInicio() { return inicio; }
    public LocalDate getFim() { return fim; }
    public SituacaoMetaAcademia getSituacao() { return situacao; }
    public Instant getCriacao() { return criacao; }
    public String getResponsavel() { return responsavel; }
}
