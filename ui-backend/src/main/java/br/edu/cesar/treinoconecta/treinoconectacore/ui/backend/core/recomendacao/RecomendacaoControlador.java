package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.recomendacao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoDetalhe;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.SerieDetalhe;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.RecomendacaoAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.RecomendacaoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.RecomendacaoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.VersaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.RestricaoFisica;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.ObjetivoTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.Evidencia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.ParametrosRecomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.PriorizacaoRecomendacoes.TaxaAceitacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.Recomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.RecomendacaoAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.RecomendacaoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.RecomendacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.ResumoAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.SugestaoSubstituicao;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "core/recomendacao")
public class RecomendacaoControlador extends AgregadoControladorAbstrato<RecomendacaoIdentificador, Recomendacao,
        RecomendacaoAutorizacoes, RecomendacaoProjecao, RecomendacaoConsulta> {
    private final RecomendacaoServico servico;
    private final RecomendacaoAplicacaoServico consultas;
    private final PlanoDeTreinoAplicacaoServico planos;

    public RecomendacaoControlador(RecomendacaoServico servico, RecomendacaoAplicacaoServico consultas,
            PlanoDeTreinoAplicacaoServico planos) {
        super(servico, consultas);
        this.servico = Objects.requireNonNull(servico);
        this.consultas = Objects.requireNonNull(consultas);
        this.planos = Objects.requireNonNull(planos);
    }

    public record Geracao(int geradas, Optional<String> aviso) {
    }

    public record Visiveis(List<RecomendacaoProjecao> recomendacoes, List<TaxaAceitacao> taxas, ResumoAluno aluno,
            int maximoExibidas) {
    }

    public record Opcao(String identificador, String nome) {
    }

    public record FaixaObjetivo(ObjetivoTreino objetivo, int seriesMinimas, int seriesMaximas, int repeticoesMinimas,
            int repeticoesMaximas) {
    }

    public record SugestaoDetalhe(String exercicioAtual, String exercicioSugerido, int series, int repeticoesMinimas,
            int repeticoesMaximas, BigDecimal carga, int intervaloSegundos, Optional<String> treino,
            Optional<SerieDetalhe> serieAtual, Optional<FaixaObjetivo> faixa) {
    }

    public record PerfilResumo(Optional<String> objetivo, Optional<String> nivel, List<RestricaoFisica> restricoes) {
    }

    public record Detalhe(RecomendacaoProjecao recomendacao, List<Evidencia> evidencias, Optional<SugestaoDetalhe> sugestao,
            List<Opcao> alternativas, PerfilResumo perfil, ResumoAluno aluno, RecomendacaoAutorizacoes autorizacoes,
            int carenciaRecusaDias) {
    }

    public record Comparacao(RecomendacaoProjecao recomendacao, PlanoDeTreinoDetalhe anterior, PlanoDeTreinoDetalhe atual,
            List<VersaoPlano> versoes) {
    }

    public record RespostaRequisicao(@Nullable String motivo) {
    }

    public record AprovacaoRequisicao(@Nullable String exercicioSugerido, @Nullable Integer series,
            @Nullable Integer repeticoesMinimas, @Nullable Integer repeticoesMaximas, @Nullable BigDecimal carga,
            @Nullable Integer intervaloSegundos, @Nullable String observacao) {
    }

    /** Gera as recomendações do aluno autenticado. Perfil incompleto ou matrícula inativa voltam como aviso. */
    @PostMapping("gerar")
    public Geracao gerar() {
        try {
            return new Geracao(servico.gerar().size(), Optional.empty());
        } catch (IllegalStateException excecao) {
            return new Geracao(0, Optional.ofNullable(excecao.getMessage()));
        }
    }

    @GetMapping("minhas")
    public Visiveis minhas() {
        var visiveis = servico.listarVisiveis().stream()
                .map(recomendacao -> consultas.projetar(recomendacao.getIdentificador().orElseThrow())).toList();
        var resumo = servico.meuResumo();
        return new Visiveis(visiveis, servico.taxasDeAceitacao(new AlunoIdentificador(resumo.aluno())), resumo,
                ParametrosRecomendacao.MAXIMO_EXIBIDAS);
    }

    @GetMapping("taxas")
    public List<TaxaAceitacao> taxas(@RequestParam AlunoIdentificador aluno) {
        return servico.taxasDeAceitacao(aluno);
    }

    @GetMapping("aluno/{aluno}")
    public ResumoAluno resumoDoAluno(@PathVariable AlunoIdentificador aluno) {
        return servico.resumo(aluno);
    }

    @GetMapping("{identificador}/detalhe")
    public Detalhe detalhe(@PathVariable RecomendacaoIdentificador identificador) {
        var recomendacao = servico.buscar(identificador);
        var autorizacoes = servico.criarAutorizacoes(Optional.of(identificador));
        var projecao = consultas.projetar(identificador);
        var perfil = servico.perfil(recomendacao.getAluno());
        var alternativas = autorizacoes.getAprovar() ? servico.alternativas(identificador).stream()
                .map(item -> new Opcao(item.exercicio().toString(), item.nome())).toList() : List.<Opcao>of();
        var sugestao = recomendacao.getSugestao().map(valor -> detalharSugestao(valor, recomendacao, projecao));
        return new Detalhe(projecao, recomendacao.getEvidencias(), sugestao, alternativas,
                new PerfilResumo(perfil.getObjetivo().map(objetivo -> objetivo.getDescricao()),
                        perfil.getNivel().map(nivel -> nivel.getDescricao()), perfil.getRestricoes()),
                servico.resumo(recomendacao.getAluno()), autorizacoes, ParametrosRecomendacao.CARENCIA_RECUSA_DIAS);
    }

    private SugestaoDetalhe detalharSugestao(SugestaoSubstituicao sugestao, Recomendacao recomendacao,
            RecomendacaoProjecao projecao) {
        Optional<String> treino = Optional.empty();
        Optional<SerieDetalhe> serieAtual = Optional.empty();
        Optional<FaixaObjetivo> faixa = Optional.empty();
        var planoTreino = recomendacao.getReferencias().planoTreino();
        if (planoTreino.isPresent()) {
            for (var item : planos.detalhe(planoTreino.get()).treinos()) {
                for (var serie : item.series()) {
                    if (serie.exercicio().equals(sugestao.exercicioAtual())) {
                        treino = Optional.of(item.nome());
                        serieAtual = Optional.of(serie);
                        var objetivo = item.objetivo();
                        faixa = Optional.of(new FaixaObjetivo(objetivo, objetivo.getSeriesMinimas(),
                                objetivo.getSeriesMaximas(), objetivo.getRepeticoesMinimas(), objetivo.getRepeticoesMaximas()));
                    }
                }
            }
        }
        return new SugestaoDetalhe(sugestao.exercicioAtual().toString(), sugestao.exercicioSugerido().toString(),
                sugestao.series(), sugestao.repeticoesMinimas(), sugestao.repeticoesMaximas(), sugestao.carga(),
                sugestao.intervaloSegundos(), treino, serieAtual, faixa);
    }

    @GetMapping("{identificador}/comparacao")
    public Comparacao comparacao(@PathVariable RecomendacaoIdentificador identificador) {
        var projecao = consultas.projetar(identificador);
        var gerado = new PlanoDeTreinoIdentificador(projecao.planoGerado().orElseThrow(
                () -> new IllegalStateException("Esta recomendação não gerou uma nova versão do plano")));
        var atual = planos.detalhe(gerado);
        var anterior = planos.detalhe(atual.planoAnterior().orElseThrow());
        return new Comparacao(projecao, anterior, atual, consultas.versoesDoPlano(gerado));
    }

    @PostMapping("{identificador}/aceitar")
    public RecomendacaoProjecao aceitar(@PathVariable RecomendacaoIdentificador identificador) {
        servico.aceitar(identificador);
        return consultas.projetar(identificador);
    }

    @PostMapping("{identificador}/recusar")
    public RecomendacaoProjecao recusar(@PathVariable RecomendacaoIdentificador identificador,
            @RequestBody RespostaRequisicao requisicao) {
        servico.recusar(identificador, Optional.ofNullable(requisicao.motivo()));
        return consultas.projetar(identificador);
    }

    @PostMapping("{identificador}/ciente")
    public RecomendacaoProjecao ciente(@PathVariable RecomendacaoIdentificador identificador) {
        servico.registrarCiencia(identificador);
        return consultas.projetar(identificador);
    }

    @PostMapping("{identificador}/aprovar")
    public RecomendacaoProjecao aprovar(@PathVariable RecomendacaoIdentificador identificador,
            @RequestBody AprovacaoRequisicao requisicao) {
        var original = servico.buscar(identificador).getSugestao();
        var ajustada = original.map(sugestao -> new SugestaoSubstituicao(sugestao.exercicioAtual(),
                requisicao.exercicioSugerido() == null ? sugestao.exercicioSugerido()
                        : new ExercicioIdentificador(requisicao.exercicioSugerido()),
                valor(requisicao.series(), sugestao.series()),
                valor(requisicao.repeticoesMinimas(), sugestao.repeticoesMinimas()),
                valor(requisicao.repeticoesMaximas(), sugestao.repeticoesMaximas()),
                requisicao.carga() == null ? sugestao.carga() : requisicao.carga(),
                valor(requisicao.intervaloSegundos(), sugestao.intervaloSegundos())));
        servico.aprovar(identificador, ajustada, Optional.ofNullable(requisicao.observacao()));
        return consultas.projetar(identificador);
    }

    private static int valor(@Nullable Integer informado, int padrao) {
        return informado == null ? padrao : informado;
    }

    @PostMapping("{identificador}/rejeitar")
    public RecomendacaoProjecao rejeitar(@PathVariable RecomendacaoIdentificador identificador,
            @RequestBody RespostaRequisicao requisicao) {
        servico.rejeitar(identificador, Optional.ofNullable(requisicao.motivo()));
        return consultas.projetar(identificador);
    }
}
