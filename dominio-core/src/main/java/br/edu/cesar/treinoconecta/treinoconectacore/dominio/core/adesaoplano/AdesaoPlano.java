package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.PeriodoCobranca;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.Plano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

/**
 * Vínculo do aluno com um plano. A periodicidade é a duração do plano no momento da adesão, para que uma alteração
 * posterior do plano não mude os períodos de cobrança já iniciados. O período coberto é [início, encerramento).
 */
public final class AdesaoPlano implements IAgregado<AdesaoPlanoIdentificador> {
    private final Optional<AdesaoPlanoIdentificador> identificador;
    private final AlunoIdentificador aluno;
    private final PlanoIdentificador plano;
    private final int periodicidadeMeses;
    private final LocalDate inicio;
    private final Optional<LocalDate> encerramento;
    private final SituacaoAdesao situacao;

    public AdesaoPlano(Optional<AdesaoPlanoIdentificador> identificador, AlunoIdentificador aluno, PlanoIdentificador plano,
            int periodicidadeMeses, LocalDate inicio, Optional<LocalDate> encerramento, SituacaoAdesao situacao) {
        this.identificador = Objects.requireNonNull(identificador);
        this.aluno = Objects.requireNonNull(aluno);
        this.plano = Objects.requireNonNull(plano);
        Validate.isTrue(periodicidadeMeses >= Plano.DURACAO_MINIMA_MESES && periodicidadeMeses <= Plano.DURACAO_MAXIMA_MESES,
                "Periodicidade de cobrança inválida");
        this.periodicidadeMeses = periodicidadeMeses;
        this.inicio = Objects.requireNonNull(inicio);
        this.encerramento = Objects.requireNonNull(encerramento);
        this.situacao = Objects.requireNonNull(situacao);
        Validate.isTrue((situacao == SituacaoAdesao.ATIVA) == encerramento.isEmpty(), "Encerramento da adesão inconsistente");
        encerramento.ifPresent(data -> Validate.isTrue(!data.isBefore(inicio), "O encerramento não pode anteceder o início"));
    }

    public static AdesaoPlano aderir(AlunoIdentificador aluno, Plano plano, LocalDate hoje) {
        Objects.requireNonNull(plano);
        Validate.validState(plano.ativo(), AdesaoPlanoServico.PLANO_INDISPONIVEL);
        return new AdesaoPlano(Optional.empty(), aluno, plano.getIdentificador().orElseThrow(), plano.getDuracaoMeses(),
                hoje, Optional.empty(), SituacaoAdesao.ATIVA);
    }

    /** Na troca, a adesão atual termina no mesmo dia em que a nova começa: os períodos não se sobrepõem. */
    public AdesaoPlano encerrarPorTroca(LocalDate hoje) {
        return finalizar(SituacaoAdesao.ENCERRADA, hoje);
    }

    /** O cancelamento só muda a situação: a adesão e as mensalidades geradas permanecem no histórico. */
    public AdesaoPlano cancelar(LocalDate hoje) {
        return finalizar(SituacaoAdesao.CANCELADA, hoje);
    }

    private AdesaoPlano finalizar(SituacaoAdesao destino, LocalDate hoje) {
        Objects.requireNonNull(hoje);
        Validate.validState(ativa(), AdesaoPlanoServico.ADESAO_NAO_ATIVA);
        var data = hoje.isBefore(inicio) ? inicio : hoje;
        return new AdesaoPlano(identificador, aluno, plano, periodicidadeMeses, inicio, Optional.of(data), destino);
    }

    public boolean ativa() {
        return situacao == SituacaoAdesao.ATIVA;
    }

    public PeriodoCobranca periodo(int indice) {
        Validate.isTrue(indice >= 0, "Índice do período inválido");
        var inicioPeriodo = inicio.plusMonths((long) indice * periodicidadeMeses);
        var fimPeriodo = inicio.plusMonths((long) (indice + 1) * periodicidadeMeses).minusDays(1);
        return new PeriodoCobranca(inicioPeriodo, fimPeriodo);
    }

    /** Períodos de cobrança já iniciados até a data informada, limitados ao encerramento da adesão. */
    public List<PeriodoCobranca> periodosIniciadosAte(LocalDate data) {
        Objects.requireNonNull(data);
        var periodos = new ArrayList<PeriodoCobranca>();
        for (int indice = 0;; indice++) {
            var periodo = periodo(indice);
            boolean depoisDoEncerramento = encerramento.map(fim -> !periodo.inicio().isBefore(fim)).orElse(false);
            if (periodo.inicio().isAfter(data) || (indice > 0 && depoisDoEncerramento)) {
                return periodos;
            }
            periodos.add(periodo);
        }
    }

    public Optional<AdesaoPlanoIdentificador> getIdentificador() { return identificador; }
    public AlunoIdentificador getAluno() { return aluno; }
    public PlanoIdentificador getPlano() { return plano; }
    public int getPeriodicidadeMeses() { return periodicidadeMeses; }
    public LocalDate getInicio() { return inicio; }
    public Optional<LocalDate> getEncerramento() { return encerramento; }
    public SituacaoAdesao getSituacao() { return situacao; }
}
