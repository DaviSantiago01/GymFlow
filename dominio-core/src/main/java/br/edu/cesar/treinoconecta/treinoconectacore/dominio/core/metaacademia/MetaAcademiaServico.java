package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.spi.IMetaAcademiaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.IndicadorAcademia;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

public final class MetaAcademiaServico
        extends AgregadoServicoAbstrato<MetaAcademiaIdentificador, MetaAcademia, MetaAcademiaAutorizacoes> {
    public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe uma meta da academia com o identificador %s";
    public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe meta da academia com o identificador %s";
    public static final String INDICADOR_OBRIGATORIO = "Escolha o indicador da meta";
    public static final String VALOR_INVALIDO = "Informe um valor desejado igual ou maior que zero";
    public static final String PERCENTUAL_INVALIDO = "Uma meta de taxa deve estar entre 0% e 100%";
    public static final String PERIODO_OBRIGATORIO = "Informe o início e o fim da meta";
    public static final String PERIODO_INVERTIDO = "O início da meta não pode ser posterior ao fim";
    public static final String PERIODO_ENCERRADO = "O período da meta deve terminar hoje ou em uma data futura";
    public static final String META_ATIVA_EXISTENTE = "Já existe uma meta ativa para este indicador no mesmo período. Encerre ou substitua a meta atual";
    public static final String META_NAO_ATIVA = "Somente uma meta ativa pode ser encerrada ou substituída";

    private final IMetaAcademiaRepositorio repositorio;
    private final Clock relogio;

    public MetaAcademiaServico(ISegurancaServico seguranca, IMetaAcademiaRepositorio repositorio, Clock relogio) {
        super(seguranca, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.relogio = Objects.requireNonNull(relogio);
    }

    @Override
    public MetaAcademiaAutorizacoes criarAutorizacoes(Optional<MetaAcademiaIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean gestor = getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name());
        return new MetaAcademiaAutorizacoes(gestor, gestor, gestor, gestor);
    }

    /** RN015: não pode existir mais de uma meta ativa para o mesmo indicador em períodos sobrepostos. */
    public MetaAcademia cadastrar(IndicadorAcademia indicador, BigDecimal valorDesejado, LocalDate inicio, LocalDate fim) {
        validarAutorizacao(Optional.empty(), MetaAcademiaAutorizacoes::getCadastrar);
        var meta = MetaAcademia.cadastrar(indicador, valorDesejado, inicio, fim, getSegurancaServico().getUsuario(),
                relogio.instant(), hoje());
        Validate.validState(!repositorio.existeAtivaSobreposta(indicador, inicio, fim, Optional.empty()),
                META_ATIVA_EXISTENTE);
        return repositorio.incluir(meta);
    }

    public MetaAcademia encerrar(MetaAcademiaIdentificador identificador) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), MetaAcademiaAutorizacoes::getEncerrar);
        validarExistencia(identificador);
        var encerrada = repositorio.bloquear(identificador).encerrar();
        repositorio.alterar(encerrada);
        return encerrada;
    }

    /** A meta atual fica no histórico como substituída, e a nova meta passa a valer para o indicador. */
    public MetaAcademia substituir(MetaAcademiaIdentificador identificador, BigDecimal valorDesejado, LocalDate inicio,
            LocalDate fim) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), MetaAcademiaAutorizacoes::getSubstituir);
        validarExistencia(identificador);
        var atual = repositorio.bloquear(identificador);
        var nova = MetaAcademia.cadastrar(atual.getIndicador(), valorDesejado, inicio, fim,
                getSegurancaServico().getUsuario(), relogio.instant(), hoje());
        Validate.validState(!repositorio.existeAtivaSobreposta(atual.getIndicador(), inicio, fim, Optional.of(identificador)),
                META_ATIVA_EXISTENTE);
        repositorio.alterar(atual.substituir());
        return repositorio.incluir(nova);
    }

    private LocalDate hoje() {
        return relogio.instant().atZone(ParametrosAcademia.FUSO).toLocalDate();
    }
}
