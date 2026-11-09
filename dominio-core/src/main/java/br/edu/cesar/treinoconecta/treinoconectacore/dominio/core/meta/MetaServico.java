package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.HistoricoDeAvaliacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.spi.IAvaliacaoFisicaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.spi.IMetaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.spi.IPersonalRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

public final class MetaServico extends AgregadoServicoAbstrato<MetaIdentificador, Meta, MetaAutorizacoes> {
    public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe uma meta com o identificador %s";
    public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe meta com o identificador %s";
    public static final String ALUNO_INEXISTENTE_MODELO = "Não existe aluno com o identificador %s";
    public static final String VALOR_DESEJADO_INVALIDO = "O valor desejado deve ser maior que zero";
    public static final String VALOR_DESEJADO_IGUAL_AO_ATUAL = "O valor desejado deve ser diferente do valor atual do indicador";
    public static final String PERIODO_OBRIGATORIO = "Informe o início e o fim do período";
    public static final String PERIODO_INVERTIDO = "O início do período não pode ser posterior ao fim";
    public static final String PERIODO_ENCERRADO = "O período da meta deve terminar hoje ou em uma data futura";
    public static final String SEM_AVALIACAO_DO_INDICADOR = "Registre uma avaliação com este indicador antes de definir a meta";
    public static final String META_ATIVA_EXISTENTE = "Já existe uma meta ativa para este indicador no período. Altere, encerre ou substitua a meta atual";
    public static final String META_NAO_ATIVA = "Somente uma meta ativa pode ser alterada";

    private final IMetaRepositorio repositorio;
    private final IAvaliacaoFisicaRepositorio avaliacoes;
    private final IAlunoRepositorio alunos;
    private final IPersonalRepositorio personais;
    private final Clock relogio;

    public MetaServico(ISegurancaServico seguranca, IMetaRepositorio repositorio, IAvaliacaoFisicaRepositorio avaliacoes,
            IAlunoRepositorio alunos, IPersonalRepositorio personais, Clock relogio) {
        super(seguranca, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.avaliacoes = Objects.requireNonNull(avaliacoes);
        this.alunos = Objects.requireNonNull(alunos);
        this.personais = Objects.requireNonNull(personais);
        this.relogio = Objects.requireNonNull(relogio);
    }

    @Override
    public MetaAutorizacoes criarAutorizacoes(Optional<MetaIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean personal = getSegurancaServico().possuiAutorizacao(Autorizacao.PERSONAL.name());
        boolean aluno = getSegurancaServico().possuiAutorizacao(Autorizacao.USUARIO.name());
        boolean buscar = personal || aluno;
        if (aluno && identificador.isPresent()) {
            var autenticado = alunos.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow();
            buscar = repositorio.buscarPorIdentificador(identificador.get()).getAluno().equals(autenticado);
        }
        return new MetaAutorizacoes(buscar, personal, personal, personal, personal);
    }

    public Meta criar(AlunoIdentificador aluno, IndicadorFisico indicador, BigDecimal valorDesejado, LocalDate inicio,
            LocalDate fim) {
        Objects.requireNonNull(aluno);
        Objects.requireNonNull(indicador);
        validarAutorizacao(Optional.empty(), MetaAutorizacoes::getCriar);
        validarExistencia(alunos::existePorIdentificador, aluno, ALUNO_INEXISTENTE_MODELO);
        bloquearAluno(aluno);
        var nova = montarMeta(aluno, indicador, valorDesejado, inicio, fim);
        validarPeriodoLivre(nova, Optional.empty());
        return repositorio.incluir(nova);
    }

    public Meta ajustar(MetaIdentificador identificador, BigDecimal valorDesejado, LocalDate inicio, LocalDate fim) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), MetaAutorizacoes::getAjustar);
        validarExistencia(identificador);
        bloquearAluno(repositorio.buscarPorIdentificador(identificador).getAluno());
        var ajustada = repositorio.bloquear(identificador).ajustar(valorDesejado, inicio, fim, hoje());
        validarPeriodoLivre(ajustada, Optional.of(identificador));
        repositorio.alterar(ajustada);
        return ajustada;
    }

    public Meta substituir(MetaIdentificador identificador, BigDecimal valorDesejado, LocalDate inicio, LocalDate fim) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), MetaAutorizacoes::getSubstituir);
        validarExistencia(identificador);
        bloquearAluno(repositorio.buscarPorIdentificador(identificador).getAluno());
        var atual = repositorio.bloquear(identificador);
        var nova = montarMeta(atual.getAluno(), atual.getIndicador(), valorDesejado, inicio, fim);
        var substituida = atual.substituir();
        validarPeriodoLivre(nova, Optional.of(identificador));
        repositorio.alterar(substituida);
        return repositorio.incluir(nova);
    }

    public Meta encerrar(MetaIdentificador identificador) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), MetaAutorizacoes::getEncerrar);
        validarExistencia(identificador);
        var encerrada = repositorio.bloquear(identificador).encerrar();
        repositorio.alterar(encerrada);
        return encerrada;
    }

    private Meta montarMeta(AlunoIdentificador aluno, IndicadorFisico indicador, BigDecimal valorDesejado, LocalDate inicio,
            LocalDate fim) {
        var historico = new HistoricoDeAvaliacao(avaliacoes.buscarValidasPorAluno(aluno));
        var referencia = historico.maisRecenteCom(indicador);
        Validate.validState(referencia.isPresent(), SEM_AVALIACAO_DO_INDICADOR);
        var personal = personais.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow();
        return Meta.criar(aluno, personal, indicador, referencia.get().valorDe(indicador).orElseThrow(), valorDesejado,
                inicio, fim, referencia.get().getIdentificador().orElseThrow(), hoje());
    }

    private void validarPeriodoLivre(Meta meta, Optional<MetaIdentificador> ignorada) {
        Validate.validState(!repositorio.existeAtivaNoPeriodo(meta.getAluno(), meta.getIndicador(), meta.getInicio(),
                meta.getFim(), ignorada), META_ATIVA_EXISTENTE);
    }

    private void bloquearAluno(AlunoIdentificador aluno) {
        alunos.bloquearPorUsuario(alunos.buscarPorIdentificador(aluno).getUsuario());
    }

    private LocalDate hoje() {
        return relogio.instant().atZone(ParametrosAcademia.FUSO).toLocalDate();
    }
}
