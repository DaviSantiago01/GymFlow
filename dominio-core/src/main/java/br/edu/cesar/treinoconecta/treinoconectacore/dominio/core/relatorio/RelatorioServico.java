package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.spi.IAulaColetivaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.spi.IMetaAcademiaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.spi.IPersonalRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.spi.IPlanoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.Resumo;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.spi.IFonteDadosRelatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.spi.IRelatorioRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;

public final class RelatorioServico extends AgregadoServicoAbstrato<RelatorioIdentificador, Relatorio, RelatorioAutorizacoes> {
    public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe um relatório com o identificador %s";
    public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe relatório com o identificador %s";
    public static final String TIPO_OBRIGATORIO = "Escolha o tipo de relatório";
    public static final String TIPO_NAO_PERMITIDO = "Personais geram apenas relatórios dos próprios alunos: risco de evasão e adesão aos treinos";
    public static final String OUTRO_PERSONAL = "O personal só pode gerar relatórios dos alunos sob sua responsabilidade";
    public static final String PERIODO_OBRIGATORIO = "Informe a data inicial e a data final do relatório";
    public static final String PERIODO_INVERTIDO = "A data inicial não pode ser posterior à data final";
    public static final String PERIODO_FUTURO = "A data final não pode ser posterior à data de hoje";
    public static final String PERIODO_LONGO = "O período de análise pode ter no máximo 12 meses";
    public static final String AGRUPAMENTO_OBRIGATORIO = "Escolha como agrupar os resultados";
    public static final String AGRUPAMENTO_MAIOR_QUE_PERIODO = "O agrupamento não pode ser maior que o próprio período";
    public static final String AGRUPAMENTO_DIARIO_LONGO_MODELO = "O agrupamento por dia só é permitido em períodos de até %d dias";
    public static final String FILTRO_INCOMPATIVEL_MODELO = "O filtro de %s não se aplica ao relatório de %s";
    public static final String FILTRO_INEXISTENTE_MODELO = "O %s informado no filtro não está cadastrado";
    public static final String SEM_DADOS_MODELO = "Não há dados de %s entre %s e %s para os filtros escolhidos. O relatório não foi gerado";
    public static final String VERSAO_JA_SUBSTITUIDA = "Este relatório já foi substituído por uma versão mais recente";

    private final IRelatorioRepositorio repositorio;
    private final IFonteDadosRelatorio fonte;
    private final IMetaAcademiaRepositorio metas;
    private final IPlanoRepositorio planos;
    private final IPersonalRepositorio personais;
    private final IAulaColetivaRepositorio aulas;
    private final Clock relogio;

    public RelatorioServico(ISegurancaServico seguranca, IRelatorioRepositorio repositorio, IFonteDadosRelatorio fonte,
            IMetaAcademiaRepositorio metas, IPlanoRepositorio planos, IPersonalRepositorio personais,
            IAulaColetivaRepositorio aulas, Clock relogio) {
        super(seguranca, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.fonte = Objects.requireNonNull(fonte);
        this.metas = Objects.requireNonNull(metas);
        this.planos = Objects.requireNonNull(planos);
        this.personais = Objects.requireNonNull(personais);
        this.aulas = Objects.requireNonNull(aulas);
        this.relogio = Objects.requireNonNull(relogio);
    }

    /** RN001: o gestor gera e consulta todos os relatórios; o personal, só os dos próprios alunos; o aluno, nenhum. */
    @Override
    public RelatorioAutorizacoes criarAutorizacoes(Optional<RelatorioIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean gestor = getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name());
        boolean personal = !gestor && getSegurancaServico().possuiAutorizacao(Autorizacao.PERSONAL.name());
        boolean buscar = gestor || personal;
        if (personal && identificador.isPresent() && repositorio.existePorIdentificador(identificador.get())) {
            buscar = repositorio.buscarPorIdentificador(identificador.get()).getUsuario()
                    .equals(getSegurancaServico().getUsuario());
        }
        List<TipoRelatorio> tipos = gestor ? Arrays.asList(TipoRelatorio.values())
                : personal ? Arrays.stream(TipoRelatorio.values()).filter(TipoRelatorio::isPermitidoAoPersonal).toList()
                        : List.of();
        return new RelatorioAutorizacoes(buscar, gestor || personal, buscar, buscar, gestor, tipos);
    }

    public Relatorio gerar(TipoRelatorio tipo, LocalDate inicio, LocalDate fim, Agrupamento agrupamento,
            FiltrosRelatorio filtros, boolean comparar) {
        validarAutorizacao(Optional.empty(), RelatorioAutorizacoes::getGerar);
        var parametros = validar(tipo, inicio, fim, agrupamento, filtros);
        var conteudo = calcular(parametros, comparar);
        var relatorio = Relatorio.gerar(tipo, parametros.periodo(), agrupamento, parametros.filtros(), comparar,
                relogio.instant(), getSegurancaServico().getUsuario(), nomeDoUsuario(), perfil(), conteudo);
        return repositorio.incluir(relatorio);
    }

    /** Informa, antes da geração, se há dados para o período e os filtros. */
    public Resumo verificar(TipoRelatorio tipo, LocalDate inicio, LocalDate fim, Agrupamento agrupamento,
            FiltrosRelatorio filtros) {
        validarAutorizacao(Optional.empty(), RelatorioAutorizacoes::getGerar);
        var parametros = validar(tipo, inicio, fim, agrupamento, filtros);
        var analise = analisar(parametros.periodo()).filtrar(parametros.filtros());
        var resumo = analise.resumo(parametros.periodo());
        validarDados(tipo, parametros.periodo(), resumo);
        return resumo;
    }

    /** RN018: recalcula com os mesmos parâmetros em uma nova versão; a anterior mantém os valores originais. */
    public Relatorio gerarNovaVersao(RelatorioIdentificador identificador) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), RelatorioAutorizacoes::getGerarNovaVersao);
        validarExistencia(identificador);
        var atual = repositorio.bloquear(identificador);
        var parametros = validar(atual.getTipo(), atual.getPeriodo().inicio(), atual.getPeriodo().fim(),
                atual.getAgrupamento(), atual.getFiltros());
        var conteudo = calcular(parametros, atual.isComparar());
        var nova = atual.novaVersao(conteudo, relogio.instant(), getSegurancaServico().getUsuario(), nomeDoUsuario(),
                perfil());
        repositorio.alterar(atual.substituir());
        return repositorio.incluir(nova);
    }

    public List<String> alteracoesAposGeracao(RelatorioIdentificador identificador) {
        var relatorio = buscar(identificador);
        return fonte.alteracoesApos(relatorio.getPeriodo(), relatorio.getGeracao());
    }

    /** Painel do mês: indicadores calculados na hora, sem gravar no histórico. */
    public PainelAcademia painel(YearMonth mes) {
        Objects.requireNonNull(mes);
        validarAutorizacao(Optional.empty(), RelatorioAutorizacoes::getPainel);
        var hoje = hoje();
        Validate.isTrue(!mes.atDay(1).isAfter(hoje), PERIODO_FUTURO);
        var fim = mes.atEndOfMonth().isAfter(hoje) ? hoje : mes.atEndOfMonth();
        var periodo = new PeriodoAnalise(mes.atDay(1), fim);
        var analise = new AnaliseAcademia(fonte.carregar(mes.minusMonths(6).atDay(1), fim), hoje);
        var vigentes = metas.buscarVigentes(periodo.inicio(), periodo.fim());
        var gerador = new GeradorConteudo(analise, periodo, true);
        var conteudo = gerador.gerar(TipoRelatorio.DESEMPENHO_GERAL, Agrupamento.SEMANA, vigentes);
        var cumprimento = gerador.gerar(TipoRelatorio.METAS_ACADEMIA, Agrupamento.SEMANA, vigentes).metas();
        var serie = new ArrayList<PainelAcademia.PontoSerie>();
        for (int indice = 5; indice >= 0; indice--) {
            var referencia = mes.minusMonths(indice);
            var data = referencia.atEndOfMonth().isAfter(hoje) ? hoje : referencia.atEndOfMonth();
            serie.add(new PainelAcademia.PontoSerie(referencia.toString(), analise.ativosEm(data).size()));
        }
        return new PainelAcademia(periodo, periodo.anterior(), conteudo, serie, cumprimento);
    }

    private record Parametros(TipoRelatorio tipo, PeriodoAnalise periodo, Agrupamento agrupamento, FiltrosRelatorio filtros) {
    }

    private Parametros validar(TipoRelatorio tipo, LocalDate inicio, LocalDate fim, Agrupamento agrupamento,
            FiltrosRelatorio filtros) {
        Validate.notNull(tipo, TIPO_OBRIGATORIO);
        Validate.notNull(agrupamento, AGRUPAMENTO_OBRIGATORIO);
        Objects.requireNonNull(filtros);
        var restritos = filtros;
        if (perfil() == Autorizacao.PERSONAL) {
            if (!tipo.isPermitidoAoPersonal()) {
                throw new NaoAutorizadoExcecao(TIPO_NAO_PERMITIDO);
            }
            var proprio = personalAutenticado();
            if (filtros.personal().filter(informado -> !informado.equals(proprio)).isPresent()) {
                throw new NaoAutorizadoExcecao(OUTRO_PERSONAL);
            }
            restritos = filtros.comPersonal(proprio);
        }
        var periodo = PeriodoAnalise.validar(inicio, fim, hoje());
        agrupamento.validarPara(periodo);
        filtros.validarPara(tipo);
        restritos.plano().ifPresent(plano -> validarExistencia(planos::existePorIdentificador, plano,
                FILTRO_INEXISTENTE_MODELO.formatted(TipoFiltro.PLANO.getDescricao())));
        restritos.personal().ifPresent(personal -> validarExistencia(personais::existePorIdentificador, personal,
                FILTRO_INEXISTENTE_MODELO.formatted(TipoFiltro.PERSONAL.getDescricao())));
        restritos.aula().ifPresent(aula -> validarExistencia(aulas::existePorIdentificador, aula,
                "A " + TipoFiltro.AULA_COLETIVA.getDescricao() + " informada no filtro não está cadastrada"));
        restritos.modalidade().ifPresent(modalidade -> Validate.isTrue(
                fonte.modalidades().stream().anyMatch(modalidade::equalsIgnoreCase),
                "A modalidade informada no filtro não está cadastrada"));
        return new Parametros(tipo, periodo, agrupamento, restritos);
    }

    private AnaliseAcademia analisar(PeriodoAnalise periodo) {
        var desde = periodo.inicio().minusDays(2 * periodo.dias());
        return new AnaliseAcademia(fonte.carregar(desde, periodo.fim()), hoje());
    }

    private ConteudoRelatorio calcular(Parametros parametros, boolean comparar) {
        var periodo = parametros.periodo();
        var analise = analisar(periodo).filtrar(parametros.filtros());
        validarDados(parametros.tipo(), periodo, analise.resumo(periodo));
        var vigentes = metas.buscarVigentes(periodo.inicio(), periodo.fim());
        return new GeradorConteudo(analise, periodo, comparar).gerar(parametros.tipo(), parametros.agrupamento(), vigentes);
    }

    private void validarDados(TipoRelatorio tipo, PeriodoAnalise periodo, Resumo resumo) {
        if (resumo.semDados(tipo)) {
            throw new IllegalStateException(SEM_DADOS_MODELO.formatted(descricaoDosDados(tipo),
                    ExportacaoRelatorio.data(periodo.inicio()), ExportacaoRelatorio.data(periodo.fim())));
        }
    }

    private static String descricaoDosDados(TipoRelatorio tipo) {
        return switch (tipo) {
        case RECEITA_INADIMPLENCIA -> "pagamentos ou mensalidades";
        case FREQUENCIA_OCUPACAO -> "entradas de alunos";
        case DESEMPENHO_PERSONAIS, ADESAO_TREINOS -> "planos de treino";
        default -> "alunos, entradas ou pagamentos";
        };
    }

    private Autorizacao perfil() {
        if (getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name())) {
            return Autorizacao.ADMINISTRADOR;
        }
        if (getSegurancaServico().possuiAutorizacao(Autorizacao.PERSONAL.name())) {
            return Autorizacao.PERSONAL;
        }
        return Autorizacao.USUARIO;
    }

    private PersonalIdentificador personalAutenticado() {
        return personais.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow();
    }

    private String nomeDoUsuario() {
        return fonte.nomeDoUsuario(getSegurancaServico().getUsuario());
    }

    private LocalDate hoje() {
        return relogio.instant().atZone(ParametrosAcademia.FUSO).toLocalDate();
    }
}
