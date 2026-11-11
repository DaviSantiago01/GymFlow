package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

public final class AvaliacaoFisica implements IAgregado<AvaliacaoFisicaIdentificador> {
    private static final BigDecimal CEM = new BigDecimal("100");

    private final Optional<AvaliacaoFisicaIdentificador> identificador;
    private final AlunoIdentificador aluno;
    private final PersonalIdentificador personal;
    private final LocalDate data;
    private final BigDecimal peso;
    private final BigDecimal altura;
    private final List<MedidaCorporal> medidas;
    private final SituacaoAvaliacao situacao;
    private final int versao;
    private final Optional<AvaliacaoFisicaIdentificador> avaliacaoAnterior;
    private final Instant registro;

    public AvaliacaoFisica(Optional<AvaliacaoFisicaIdentificador> identificador, AlunoIdentificador aluno,
            PersonalIdentificador personal, LocalDate data, BigDecimal peso, BigDecimal altura,
            List<MedidaCorporal> medidas, SituacaoAvaliacao situacao, int versao,
            Optional<AvaliacaoFisicaIdentificador> avaliacaoAnterior, Instant registro) {
        this.identificador = Objects.requireNonNull(identificador);
        this.aluno = Objects.requireNonNull(aluno);
        this.personal = Objects.requireNonNull(personal);
        this.data = Validate.notNull(data, AvaliacaoFisicaServico.DATA_OBRIGATORIA);
        Validate.notNull(peso, AvaliacaoFisicaServico.PESO_OBRIGATORIO);
        Validate.notNull(altura, AvaliacaoFisicaServico.ALTURA_OBRIGATORIA);
        validarLimites(peso, altura);
        this.peso = peso.setScale(2, RoundingMode.HALF_UP);
        this.altura = altura.setScale(2, RoundingMode.HALF_UP);
        this.medidas = List.copyOf(medidas);
        validarMedidasUnicas(this.medidas);
        this.situacao = Objects.requireNonNull(situacao);
        Validate.isTrue(versao >= 1, AvaliacaoFisicaServico.VERSAO_INVALIDA);
        this.versao = versao;
        this.avaliacaoAnterior = Objects.requireNonNull(avaliacaoAnterior);
        this.registro = Objects.requireNonNull(registro);
    }

    private static void validarLimites(BigDecimal peso, BigDecimal altura) {
        Validate.isTrue(peso.compareTo(ParametrosAcademia.PESO_MINIMO) >= 0
                && peso.compareTo(ParametrosAcademia.PESO_MAXIMO) <= 0,
                AvaliacaoFisicaServico.PESO_FORA_DOS_LIMITES_MODELO, ParametrosAcademia.PESO_MINIMO,
                ParametrosAcademia.PESO_MAXIMO);
        Validate.isTrue(altura.compareTo(ParametrosAcademia.ALTURA_MINIMA) >= 0
                && altura.compareTo(ParametrosAcademia.ALTURA_MAXIMA) <= 0,
                AvaliacaoFisicaServico.ALTURA_FORA_DOS_LIMITES_MODELO, ParametrosAcademia.ALTURA_MINIMA,
                ParametrosAcademia.ALTURA_MAXIMA);
    }

    private static void validarMedidasUnicas(List<MedidaCorporal> medidas) {
        var indicadores = new HashSet<IndicadorFisico>();
        for (var medida : medidas) {
            Validate.isTrue(indicadores.add(medida.indicador()), AvaliacaoFisicaServico.MEDIDA_REPETIDA_MODELO,
                    medida.indicador());
        }
    }

    public static AvaliacaoFisica registrar(AlunoIdentificador aluno, PersonalIdentificador personal, LocalDate data,
            BigDecimal peso, BigDecimal altura, List<MedidaCorporal> medidas, LocalDate hoje, Instant agora) {
        var avaliacao = new AvaliacaoFisica(Optional.empty(), aluno, personal, data, peso, altura, medidas,
                SituacaoAvaliacao.VALIDA, 1, Optional.empty(), agora);
        Validate.isTrue(!data.isAfter(hoje), AvaliacaoFisicaServico.DATA_FUTURA);
        return avaliacao;
    }

    /** Monta a nova versão. A avaliação atual permanece igual até ser substituída. */
    public AvaliacaoFisica corrigir(PersonalIdentificador responsavel, LocalDate novaData, BigDecimal novoPeso,
            BigDecimal novaAltura, List<MedidaCorporal> novasMedidas, LocalDate hoje, Instant agora) {
        var corrigida = new AvaliacaoFisica(Optional.empty(), aluno, responsavel, novaData, novoPeso, novaAltura,
                novasMedidas, SituacaoAvaliacao.VALIDA, versao + 1, identificador, agora);
        Validate.isTrue(!novaData.isAfter(hoje), AvaliacaoFisicaServico.DATA_FUTURA);
        Validate.validState(isValida(), AvaliacaoFisicaServico.AVALIACAO_NAO_VALIDA);
        return corrigida;
    }

    public AvaliacaoFisica substituir() {
        Validate.validState(isValida(), AvaliacaoFisicaServico.AVALIACAO_NAO_VALIDA);
        return comSituacao(SituacaoAvaliacao.SUBSTITUIDA);
    }

    public AvaliacaoFisica excluir() {
        Validate.validState(isValida(), AvaliacaoFisicaServico.AVALIACAO_NAO_VALIDA);
        return comSituacao(SituacaoAvaliacao.EXCLUIDA);
    }

    private AvaliacaoFisica comSituacao(SituacaoAvaliacao novaSituacao) {
        return new AvaliacaoFisica(identificador, aluno, personal, data, peso, altura, medidas, novaSituacao, versao,
                avaliacaoAnterior, registro);
    }

    public boolean isValida() {
        return situacao == SituacaoAvaliacao.VALIDA;
    }

    public BigDecimal getImc() {
        var alturaEmMetros = altura.divide(CEM, 4, RoundingMode.HALF_UP);
        return peso.divide(alturaEmMetros.multiply(alturaEmMetros), 2, RoundingMode.HALF_UP);
    }

    public ClassificacaoImc getClassificacaoImc() {
        return ClassificacaoImc.classificar(getImc());
    }

    /** Peso e IMC sempre existem; as medidas dependem do que foi informado. */
    public boolean existeValor(IndicadorFisico indicador) {
        Objects.requireNonNull(indicador);
        if (indicador == IndicadorFisico.PESO || indicador == IndicadorFisico.IMC) {
            return true;
        }
        for (var medida : medidas) {
            if (medida.indicador() == indicador) {
                return true;
            }
        }
        return false;
    }

    public BigDecimal buscarValor(IndicadorFisico indicador) {
        Objects.requireNonNull(indicador);
        if (indicador == IndicadorFisico.PESO) {
            return peso;
        }
        if (indicador == IndicadorFisico.IMC) {
            return getImc();
        }
        for (var medida : medidas) {
            if (medida.indicador() == indicador) {
                return medida.valor();
            }
        }
        throw new NoSuchElementException(String.format(AvaliacaoFisicaServico.VALOR_INEXISTENTE_MODELO, indicador));
    }

    public List<ComparacaoIndicador> compararCom(AvaliacaoFisica referencia) {
        Objects.requireNonNull(referencia);
        var comparacoes = new ArrayList<ComparacaoIndicador>();
        for (var indicador : IndicadorFisico.values()) {
            if (referencia.existeValor(indicador) && existeValor(indicador)) {
                comparacoes.add(ComparacaoIndicador.comparar(indicador, referencia.buscarValor(indicador),
                        buscarValor(indicador)));
            }
        }
        return comparacoes;
    }

    public Optional<AvaliacaoFisicaIdentificador> getIdentificador() { return identificador; }
    public AlunoIdentificador getAluno() { return aluno; }
    public PersonalIdentificador getPersonal() { return personal; }
    public LocalDate getData() { return data; }
    public BigDecimal getPeso() { return peso; }
    public BigDecimal getAltura() { return altura; }
    public List<MedidaCorporal> getMedidas() { return medidas; }
    public SituacaoAvaliacao getSituacao() { return situacao; }
    public int getVersao() { return versao; }
    public Optional<AvaliacaoFisicaIdentificador> getAvaliacaoAnterior() { return avaliacaoAnterior; }
    public Instant getRegistro() { return registro; }
}
