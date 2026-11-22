package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.spi.IAdesaoPlanoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.AulaColetivaServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.spi.IAulaColetivaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisica;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.spi.IAvaliacaoFisicaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.spi.IExecucaoDeExercicioRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.spi.IMetaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.PerfilAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.PerfilAlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.spi.IPerfilAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.spi.IPersonalRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.spi.IPlanoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.Serie;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.SituacaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.Treino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.spi.IPlanoDeTreinoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.spi.ICatalogoRecomendacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.spi.IRecomendacaoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;

public final class RecomendacaoServico
        extends AgregadoServicoAbstrato<RecomendacaoIdentificador, Recomendacao, RecomendacaoAutorizacoes> {
    public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe uma recomendação com o identificador %s";
    public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe recomendação com o identificador %s";
    public static final String ALUNO_INATIVO = "Recomendações são geradas apenas para alunos com matrícula ativa";
    public static final String PERFIL_INCOMPLETO_MODELO = "Complete seu perfil para receber recomendações personalizadas. Falta informar: %s";
    public static final String SEM_JUSTIFICATIVA = "Toda recomendação precisa registrar o critério e os dados que a fundamentaram";
    public static final String SEM_PERSONAL_RESPONSAVEL = "Sugestões de treino precisam do personal e do plano de treino responsáveis";
    public static final String RECOMENDACAO_DE_OUTRO_ALUNO = "Esta recomendação pertence a outro aluno";
    public static final String SUGESTAO_DE_OUTRO_PERSONAL = "Esta sugestão está sob a responsabilidade de outro personal";
    public static final String NAO_PENDENTE = "Esta recomendação já foi respondida";
    public static final String NAO_AGUARDA_APROVACAO = "Esta sugestão já foi analisada";
    public static final String NAO_APROVADA = "Somente mudanças aprovadas pelo personal podem ser confirmadas";
    public static final String RECOMENDACAO_EXPIRADA = "O prazo desta recomendação terminou";
    public static final String MESMO_EXERCICIO = "Escolha um exercício diferente do atual";
    public static final String TEXTO_LONGO = "Use no máximo 250 caracteres";
    public static final String PLANO_ALTERADO = "O plano de treino mudou desde a sugestão. Rejeite esta sugestão e analise o plano atual";
    public static final String EXERCICIO_FORA_DO_PLANO = "O exercício sugerido para troca não está mais no plano ativo";
    public static final String SUBSTITUTO_INVALIDO_MODELO = "O exercício escolhido não pode substituir o atual: %s";
    public static final String SUBSTITUTO_GRUPO_DIFERENTE = "trabalha outro grupo muscular";
    public static final String SUBSTITUTO_JA_NO_PLANO = "já faz parte do plano";
    public static final String SUBSTITUTO_SEM_CLASSIFICACAO = "não tem classificação cadastrada";

    private final IRecomendacaoRepositorio repositorio;
    private final IPerfilAlunoRepositorio perfis;
    private final IAlunoRepositorio alunos;
    private final IPersonalRepositorio personais;
    private final IAdesaoPlanoRepositorio adesoes;
    private final IPlanoRepositorio planos;
    private final IPlanoDeTreinoRepositorio planosDeTreino;
    private final IExecucaoDeExercicioRepositorio execucoes;
    private final IAvaliacaoFisicaRepositorio avaliacoes;
    private final IMetaRepositorio metas;
    private final IAulaColetivaRepositorio aulas;
    private final ICatalogoRecomendacaoRepositorio catalogo;
    private final Clock relogio;

    public RecomendacaoServico(ISegurancaServico seguranca, IRecomendacaoRepositorio repositorio,
            IPerfilAlunoRepositorio perfis, IAlunoRepositorio alunos, IPersonalRepositorio personais,
            IAdesaoPlanoRepositorio adesoes, IPlanoRepositorio planos, IPlanoDeTreinoRepositorio planosDeTreino,
            IExecucaoDeExercicioRepositorio execucoes, IAvaliacaoFisicaRepositorio avaliacoes, IMetaRepositorio metas,
            IAulaColetivaRepositorio aulas, ICatalogoRecomendacaoRepositorio catalogo, Clock relogio) {
        super(seguranca, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.perfis = Objects.requireNonNull(perfis);
        this.alunos = Objects.requireNonNull(alunos);
        this.personais = Objects.requireNonNull(personais);
        this.adesoes = Objects.requireNonNull(adesoes);
        this.planos = Objects.requireNonNull(planos);
        this.planosDeTreino = Objects.requireNonNull(planosDeTreino);
        this.execucoes = Objects.requireNonNull(execucoes);
        this.avaliacoes = Objects.requireNonNull(avaliacoes);
        this.metas = Objects.requireNonNull(metas);
        this.aulas = Objects.requireNonNull(aulas);
        this.catalogo = Objects.requireNonNull(catalogo);
        this.relogio = Objects.requireNonNull(relogio);
    }

    @Override
    public RecomendacaoAutorizacoes criarAutorizacoes(Optional<RecomendacaoIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean aluno = getSegurancaServico().possuiAutorizacao(Autorizacao.USUARIO.name());
        boolean personal = getSegurancaServico().possuiAutorizacao(Autorizacao.PERSONAL.name());
        boolean buscar = aluno || personal;
        if (identificador.isPresent() && repositorio.existePorIdentificador(identificador.get())) {
            var recomendacao = repositorio.buscarPorIdentificador(identificador.get());
            if (aluno) {
                buscar = recomendacao.getAluno().equals(alunoAutenticado());
            } else if (personal) {
                buscar = recomendacao.getReferencias().personal().filter(personalAutenticado()::equals).isPresent()
                        || planosDeTreino.existeAtivoPorAluno(recomendacao.getAluno())
                                && planosDeTreino.buscarAtivoPorAluno(recomendacao.getAluno()).getPersonal()
                                        .equals(personalAutenticado());
            }
        }
        return new RecomendacaoAutorizacoes(buscar, aluno, aluno && buscar, personal && buscar);
    }

    /**
     * Gera as recomendações do aluno autenticado. RN001 e RN002: exige matrícula ativa e perfil mínimo. RN013, RN014 e
     * RN017: descarta duplicadas, recusadas na carência e de categorias desativadas.
     */
    public List<Recomendacao> gerar() {
        validarAutorizacao(Optional.empty(), RecomendacaoAutorizacoes::getGerar);
        var aluno = alunoAutenticado();
        alunos.bloquearPorUsuario(getSegurancaServico().getUsuario());
        expirarVencidas(aluno);
        var adesao = adesoes.buscarAtivaDoAluno(aluno);
        Validate.validState(adesao.isPresent(), ALUNO_INATIVO);
        var perfil = perfil(aluno);
        Validate.validState(perfil.completo(), PERFIL_INCOMPLETO_MODELO, String.join(", ", perfil.camposPendentes()));
        var hoje = hoje();
        var historico = repositorio.buscarPorAluno(aluno);
        var contexto = contexto(perfil, adesao.get().getPlano(), adesao.get().getInicio(), hoje);
        var avaliacoesDoAluno = contexto.avaliacoes();
        var geradas = new ArrayList<Recomendacao>();
        for (var candidata : new MotorRecomendacoes(contexto, relogio.instant()).gerar()) {
            if (!perfil.aceita(candidata.getCategoria())) {
                continue;
            }
            if (duplicada(candidata, historico) || emCarencia(candidata, historico, avaliacoesDoAluno, hoje)) {
                continue;
            }
            geradas.add(repositorio.incluir(candidata));
        }
        return geradas;
    }

    private static boolean duplicada(Recomendacao candidata, List<Recomendacao> historico) {
        return historico.stream().anyMatch(existente -> existente.getSituacao().emAberto()
                && existente.getCategoria() == candidata.getCategoria() && existente.getItem().equals(candidata.getItem()));
    }

    /** RN014: recusada há menos que a carência, salvo se houve nova avaliação física depois da recusa. */
    private static boolean emCarencia(Recomendacao candidata, List<Recomendacao> historico,
            List<AvaliacaoFisica> avaliacoesDoAluno,
            LocalDate hoje) {
        for (var existente : historico) {
            if (!existente.getSituacao().recusada() || !existente.getItem().equals(candidata.getItem())
                    || existente.getCategoria() != candidata.getCategoria()) {
                continue;
            }
            var recusa = existente.getResposta().orElseThrow().instante();
            var diaDaRecusa = recusa.atZone(ParametrosAcademia.FUSO).toLocalDate();
            if (diaDaRecusa.plusDays(ParametrosRecomendacao.CARENCIA_RECUSA_DIAS).isBefore(hoje)) {
                continue;
            }
            boolean dadosMudaram = avaliacoesDoAluno.stream().anyMatch(avaliacao -> avaliacao.getRegistro().isAfter(recusa));
            if (!dadosMudaram) {
                return true;
            }
        }
        return false;
    }

    private ContextoRecomendacao contexto(PerfilAluno perfil, PlanoIdentificador planoContratado,
            LocalDate inicioAdesao, LocalDate hoje) {
        var aluno = perfil.getAluno();
        Optional<PlanoDeTreino> planoAtivo = planosDeTreino.existeAtivoPorAluno(aluno)
                ? Optional.of(planosDeTreino.buscarAtivoPorAluno(aluno)) : Optional.empty();
        var execucoesPorExercicio = new HashMap<ExercicioIdentificador, List<ExecucaoDeExercicio>>();
        planoAtivo.ifPresent(plano -> plano.getTreinos().forEach(treino -> treino.series().forEach(serie -> execucoesPorExercicio
                .put(serie.exercicio(), execucoes.buscarPorAlunoEExercicio(aluno, serie.exercicio())))));
        var desde = hoje.minusWeeks(ParametrosRecomendacao.SEMANAS_EXIBIDAS_NA_JUSTIFICATIVA + 1);
        return new ContextoRecomendacao(perfil, hoje, planoAtivo, execucoesPorExercicio,
                avaliacoes.buscarValidasPorAluno(aluno), metas.buscarAtivasPorAluno(aluno),
                catalogo.entradasDoAluno(aluno, desde), catalogo.entradasDaAcademia(desde, hoje),
                catalogo.exerciciosClassificados(), aulas.buscarAtivas(), aulas.buscarInscricoesDoAluno(aluno),
                Optional.of(planos.buscarPorIdentificador(planoContratado)), Optional.of(inicioAdesao),
                catalogo.planosAtivos());
    }

    /** RN015: recomendações em aberto e vencidas passam a expiradas. */
    private void expirarVencidas(AlunoIdentificador aluno) {
        var hoje = hoje();
        for (var recomendacao : repositorio.buscarPorAluno(aluno)) {
            if (recomendacao.vencidaEm(hoje)) {
                repositorio.alterar(repositorio.bloquear(recomendacao.getIdentificador().orElseThrow()).expirar(hoje));
            }
        }
    }

    /** RN016 e RN018: recomendações exibidas ao aluno, priorizadas e limitadas. */
    public List<Recomendacao> listarVisiveis() {
        validarAutorizacao(Optional.empty(), RecomendacaoAutorizacoes::getBuscar);
        var aluno = alunoAutenticado();
        var hoje = hoje();
        var historico = repositorio.buscarPorAluno(aluno);
        var visiveis = historico.stream().filter(recomendacao -> recomendacao.visivelAoAluno(hoje)).toList();
        return new PriorizacaoRecomendacoes(historico).ordenar(visiveis);
    }

    public List<PriorizacaoRecomendacoes.TaxaAceitacao> taxasDeAceitacao(AlunoIdentificador aluno) {
        Objects.requireNonNull(aluno);
        validarAcessoAoAluno(aluno);
        return new PriorizacaoRecomendacoes(repositorio.buscarPorAluno(aluno)).taxas();
    }

    private void validarAcessoAoAluno(AlunoIdentificador aluno) {
        validarAutorizacao(Optional.empty(), RecomendacaoAutorizacoes::getBuscar);
        if (getSegurancaServico().possuiAutorizacao(Autorizacao.USUARIO.name()) && !aluno.equals(alunoAutenticado())) {
            throw new NaoAutorizadoExcecao(RECOMENDACAO_DE_OUTRO_ALUNO);
        }
    }

    /** Aceite pelo aluno. Ao aceitar uma aula, ele é inscrito nela, respeitando vagas e conflitos (RN006). */
    public Recomendacao aceitar(RecomendacaoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), RecomendacaoAutorizacoes::getResponder);
        validarExistencia(identificador);
        var aluno = alunos.buscarPorUsuario(getSegurancaServico().getUsuario());
        var alunoId = aluno.getIdentificador().orElseThrow();
        var aceita = repositorio.bloquear(identificador).aceitar(alunoId, aluno.getNome(), relogio.instant(), hoje());
        var aula = aceita.getReferencias().aula();
        if (aceita.getCriterio() == CriterioRecomendacao.AULA_COMPATIVEL && aula.isPresent()) {
            var bloqueada = aulas.bloquear(aula.get());
            for (var inscrita : aulas.buscarInscricoesDoAluno(alunoId)) {
                Validate.validState(!bloqueada.conflitaCom(inscrita), AulaColetivaServico.CONFLITO_MODELO, inscrita.getNome());
            }
            aulas.alterar(bloqueada.inscrever(alunoId));
        }
        repositorio.alterar(aceita);
        return aceita;
    }

    /** Recusa pelo aluno. O item fica fora das próximas recomendações durante a carência (RN014). */
    public Recomendacao recusar(RecomendacaoIdentificador identificador, Optional<String> motivo) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), RecomendacaoAutorizacoes::getResponder);
        validarExistencia(identificador);
        var aluno = alunos.buscarPorUsuario(getSegurancaServico().getUsuario());
        var recusada = repositorio.bloquear(identificador).recusar(aluno.getIdentificador().orElseThrow(), aluno.getNome(),
                motivo, relogio.instant(), hoje());
        repositorio.alterar(recusada);
        return recusada;
    }

    public Recomendacao registrarCiencia(RecomendacaoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), RecomendacaoAutorizacoes::getResponder);
        validarExistencia(identificador);
        var ciente = repositorio.bloquear(identificador).registrarCiencia(alunoAutenticado(), relogio.instant());
        repositorio.alterar(ciente);
        return ciente;
    }

    /**
     * RN011 e RN020: o personal aprova a sugestão, com ou sem ajustes. Na substituição de exercício, a alteração gera
     * uma nova versão do plano de treino, e a versão anterior é preservada como substituída.
     */
    public Recomendacao aprovar(RecomendacaoIdentificador identificador, Optional<SugestaoSubstituicao> ajustada,
            Optional<String> observacao) {
        Objects.requireNonNull(identificador);
        Objects.requireNonNull(ajustada);
        validarAutorizacao(Optional.of(identificador), RecomendacaoAutorizacoes::getAprovar);
        validarExistencia(identificador);
        var personal = personais.buscarPorUsuario(getSegurancaServico().getUsuario());
        var personalId = personal.getIdentificador().orElseThrow();
        var recomendacao = repositorio.bloquear(identificador);
        var agora = relogio.instant();
        if (recomendacao.getCriterio() != CriterioRecomendacao.SUBSTITUICAO_EXERCICIO) {
            var aprovada = recomendacao.aprovar(personalId, personal.getNome(), observacao, Optional.empty(), agora, hoje());
            repositorio.alterar(aprovada);
            return aprovada;
        }
        var original = recomendacao.getSugestao().orElseThrow();
        var sugestao = ajustada.orElse(original);
        Validate.isTrue(sugestao.exercicioAtual().equals(original.exercicioAtual()), EXERCICIO_FORA_DO_PLANO);
        alunos.bloquearPorUsuario(alunos.buscarPorIdentificador(recomendacao.getAluno()).getUsuario());
        var plano = planosDeTreino.bloquear(recomendacao.getReferencias().planoTreino().orElseThrow());
        Validate.validState(plano.getSituacao() == SituacaoPlano.ATIVO, PLANO_ALTERADO);
        validarSubstituto(plano, sugestao, perfil(recomendacao.getAluno()));
        var revisado = plano.revisar(personalId, plano.getNome(), plano.getFrequenciaSemanal(),
                treinosComSubstituicao(plano, sugestao), agora);
        planosDeTreino.alterar(plano.substituir(personalId, agora));
        var novaVersao = planosDeTreino.incluir(revisado);
        var ajuste = observacao.or(() -> descreverAjuste(original, sugestao));
        var aprovada = recomendacao.aprovar(personalId, personal.getNome(), ajuste, novaVersao.getIdentificador(), agora,
                hoje());
        repositorio.alterar(aprovada);
        return aprovada;
    }

    private void validarSubstituto(PlanoDeTreino plano, SugestaoSubstituicao sugestao, PerfilAluno perfil) {
        boolean noPlano = plano.getTreinos().stream().anyMatch(treino -> treino.existeSerie(sugestao.exercicioAtual()));
        Validate.validState(noPlano, EXERCICIO_FORA_DO_PLANO);
        var classificados = catalogo.exerciciosClassificados();
        var atual = classificados.stream().filter(item -> item.exercicio().equals(sugestao.exercicioAtual())).findFirst();
        var substituto = classificados.stream().filter(item -> item.exercicio().equals(sugestao.exercicioSugerido()))
                .findFirst();
        Validate.isTrue(substituto.isPresent() && atual.isPresent(), SUBSTITUTO_INVALIDO_MODELO, SUBSTITUTO_SEM_CLASSIFICACAO);
        Validate.isTrue(substituto.get().grupoMuscular() == atual.get().grupoMuscular(), SUBSTITUTO_INVALIDO_MODELO,
                SUBSTITUTO_GRUPO_DIFERENTE);
        Validate.isTrue(plano.getTreinos().stream().noneMatch(treino -> treino.existeSerie(sugestao.exercicioSugerido())),
                SUBSTITUTO_INVALIDO_MODELO, SUBSTITUTO_JA_NO_PLANO);
        substituto.get().incompatibilidadeCom(perfil).ifPresent(motivo -> {
            throw new IllegalArgumentException(SUBSTITUTO_INVALIDO_MODELO.formatted(motivo.toLowerCase()));
        });
    }

    private static List<Treino> treinosComSubstituicao(PlanoDeTreino plano, SugestaoSubstituicao sugestao) {
        var treinos = new ArrayList<Treino>();
        for (var treino : plano.getTreinos()) {
            var series = new ArrayList<Serie>();
            for (var serie : treino.series()) {
                series.add(serie.exercicio().equals(sugestao.exercicioAtual()) ? sugestao.paraSerie() : serie);
            }
            treinos.add(new Treino(treino.nome(), treino.dia(), treino.objetivo(), series));
        }
        return treinos;
    }

    private static Optional<String> descreverAjuste(SugestaoSubstituicao original, SugestaoSubstituicao aprovada) {
        var ajustes = new ArrayList<String>();
        if (!original.exercicioSugerido().equals(aprovada.exercicioSugerido())) {
            ajustes.add("outro exercício substituto");
        }
        if (original.series() != aprovada.series()) {
            ajustes.add("séries de " + original.series() + " para " + aprovada.series());
        }
        if (original.repeticoesMinimas() != aprovada.repeticoesMinimas()
                || original.repeticoesMaximas() != aprovada.repeticoesMaximas()) {
            ajustes.add("repetições de " + original.repeticoesMinimas() + "–" + original.repeticoesMaximas() + ", como sugerido, para "
                    + aprovada.repeticoesMinimas() + "–" + aprovada.repeticoesMaximas());
        }
        if (original.carga().compareTo(aprovada.carga()) != 0) {
            ajustes.add("carga de " + texto(original.carga()) + " kg para " + texto(aprovada.carga()) + " kg");
        }
        if (original.intervaloSegundos() != aprovada.intervaloSegundos()) {
            ajustes.add("intervalo de " + original.intervaloSegundos() + " s para " + aprovada.intervaloSegundos() + " s");
        }
        if (ajustes.isEmpty()) {
            return Optional.empty();
        }
        var texto = String.join("; ", ajustes);
        return Optional.of(texto.substring(0, 1).toUpperCase() + texto.substring(1) + ".");
    }

    private static String texto(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString().replace('.', ',');
    }

    public Recomendacao rejeitar(RecomendacaoIdentificador identificador, Optional<String> motivo) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), RecomendacaoAutorizacoes::getAprovar);
        validarExistencia(identificador);
        var personal = personais.buscarPorUsuario(getSegurancaServico().getUsuario());
        var rejeitada = repositorio.bloquear(identificador).rejeitar(personal.getIdentificador().orElseThrow(),
                personal.getNome(), motivo, relogio.instant(), hoje());
        repositorio.alterar(rejeitada);
        return rejeitada;
    }

    /** Matrícula e plano de treino ativo do aluno autenticado ou, para o personal, de um aluno que ele acompanha. */
    public ResumoAluno resumo(AlunoIdentificador aluno) {
        Objects.requireNonNull(aluno);
        validarAcessoAoAluno(aluno);
        var dados = alunos.buscarPorIdentificador(aluno);
        Optional<ResumoAluno.PlanoTreinoResumido> plano = Optional.empty();
        if (planosDeTreino.existeAtivoPorAluno(aluno)) {
            var ativo = planosDeTreino.buscarAtivoPorAluno(aluno);
            var personal = personais.buscarPorIdentificador(ativo.getPersonal());
            plano = Optional.of(new ResumoAluno.PlanoTreinoResumido(ativo.getIdentificador().orElseThrow().toString(),
                    ativo.getNome(), ativo.getVersao(), ativo.getPersonal().toString(), personal.getNome(),
                    ativo.getFrequenciaSemanal(), ativo.getTreinos().stream().map(Treino::dia).sorted().toList()));
        }
        return new ResumoAluno(aluno.toString(), dados.getNome(), adesoes.buscarAtivaDoAluno(aluno).isPresent(), plano);
    }

    public ResumoAluno meuResumo() {
        return resumo(alunoAutenticado());
    }

    /** Exercícios que podem substituir o atual: mesmo grupo muscular, fora do plano e compatíveis com o aluno. */
    public List<ExercicioClassificado> alternativas(RecomendacaoIdentificador identificador) {
        var recomendacao = buscar(identificador);
        var sugestao = recomendacao.getSugestao();
        var planoId = recomendacao.getReferencias().planoTreino();
        if (sugestao.isEmpty() || planoId.isEmpty()) {
            return List.of();
        }
        var plano = planosDeTreino.buscarPorIdentificador(planoId.get());
        var perfil = perfil(recomendacao.getAluno());
        var classificados = catalogo.exerciciosClassificados();
        var atual = classificados.stream().filter(item -> item.exercicio().equals(sugestao.get().exercicioAtual()))
                .findFirst();
        if (atual.isEmpty()) {
            return List.of();
        }
        return classificados.stream().filter(item -> item.grupoMuscular() == atual.get().grupoMuscular())
                .filter(item -> plano.getTreinos().stream().noneMatch(treino -> treino.existeSerie(item.exercicio())))
                .filter(item -> item.incompatibilidadeCom(perfil).isEmpty()).toList();
    }

    public PerfilAluno perfil(AlunoIdentificador aluno) {
        var identificador = new PerfilAlunoIdentificador(aluno.toString());
        return perfis.existePorIdentificador(identificador) ? perfis.buscarPorIdentificador(identificador)
                : PerfilAluno.vazio(aluno, relogio.instant());
    }

    private AlunoIdentificador alunoAutenticado() {
        return alunos.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow();
    }

    private PersonalIdentificador personalAutenticado() {
        return personais.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow();
    }

    private LocalDate hoje() {
        return relogio.instant().atZone(ParametrosAcademia.FUSO).toLocalDate();
    }
}
