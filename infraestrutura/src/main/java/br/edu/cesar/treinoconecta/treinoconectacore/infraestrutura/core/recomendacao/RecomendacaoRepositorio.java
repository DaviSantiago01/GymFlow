package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.recomendacao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.AulaColetivaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.Evidencia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.Recomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.RecomendacaoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.RecomendacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.ReferenciasRecomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.RespostaRecomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.SugestaoSubstituicao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.spi.IRecomendacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.exercicio.IExercicioRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.IPersonalRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.planodetreino.IPlanoDeTreinoRepositorioJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class RecomendacaoRepositorio
        extends AgregadoRepositorioAbstrato<RecomendacaoIdentificador, Recomendacao, Long, RecomendacaoJpa>
        implements IRecomendacaoRepositorio {
    private final IRecomendacaoRepositorioJpa repositorioJpa;
    private final IAlunoRepositorioJpa alunos;
    private final IPersonalRepositorioJpa personais;
    private final IPlanoDeTreinoRepositorioJpa planos;
    private final IExercicioRepositorioJpa exercicios;

    public RecomendacaoRepositorio(IRecomendacaoRepositorioJpa repositorioJpa, IAlunoRepositorioJpa alunos,
            IPersonalRepositorioJpa personais, IPlanoDeTreinoRepositorioJpa planos, IExercicioRepositorioJpa exercicios) {
        super(repositorioJpa, RecomendacaoServico.IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
        this.alunos = Objects.requireNonNull(alunos);
        this.personais = Objects.requireNonNull(personais);
        this.planos = Objects.requireNonNull(planos);
        this.exercicios = Objects.requireNonNull(exercicios);
    }

    @Override
    protected Long paraIdentificadorJpa(RecomendacaoIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    private static Long codigo(Object identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected RecomendacaoJpa paraJpa(Recomendacao agregado) {
        var referencias = agregado.getReferencias();
        var entidade = new RecomendacaoJpa(agregado.getIdentificador().map(this::paraIdentificadorJpa).orElse(null),
                alunos.getReferenceById(codigo(agregado.getAluno())), agregado.getCategoria(), agregado.getCriterio(),
                agregado.getItem(), agregado.getTitulo(), agregado.getDescricao(), agregado.getJustificativa(),
                referencias.personal().map(id -> personais.getReferenceById(codigo(id))).orElse(null),
                referencias.planoTreino().map(id -> planos.getReferenceById(codigo(id))).orElse(null), agregado.getGeracao(),
                agregado.getValidade(), agregado.getSituacao());
        agregado.getSugestao().ifPresentOrElse(sugestao -> entidade.definirSugestao(
                exercicios.getReferenceById(codigo(sugestao.exercicioAtual())),
                exercicios.getReferenceById(codigo(sugestao.exercicioSugerido())), sugestao.series(),
                sugestao.repeticoesMinimas(), sugestao.repeticoesMaximas(), sugestao.carga(), sugestao.intervaloSegundos()),
                () -> entidade.definirSugestao(null, null, null, null, null, null, null));
        entidade.definirReferencias(referencias.aula().map(RecomendacaoRepositorio::codigo).orElse(null),
                referencias.plano().map(RecomendacaoRepositorio::codigo).orElse(null));
        var resposta = agregado.getResposta();
        entidade.definirSituacao(agregado.getGeracao(), agregado.getValidade(), agregado.getSituacao(),
                resposta.map(RespostaRecomendacao::instante).orElse(null),
                resposta.map(RespostaRecomendacao::respondente).orElse(null),
                resposta.flatMap(RespostaRecomendacao::motivo).orElse(null),
                resposta.flatMap(RespostaRecomendacao::ajuste).orElse(null),
                agregado.getPlanoGerado().map(id -> planos.getReferenceById(codigo(id))).orElse(null),
                agregado.getCiencia().orElse(null));
        var evidencias = new ArrayList<EvidenciaJpa>();
        for (var evidencia : agregado.getEvidencias()) {
            evidencias.add(new EvidenciaJpa(evidencia.rotulo(), evidencia.valor(), evidencia.destaque()));
        }
        entidade.definirEvidencias(evidencias);
        return entidade;
    }

    @Override
    protected Recomendacao paraAgregado(RecomendacaoJpa entidade) {
        var personal = Optional.ofNullable(entidade.getPersonal())
                .map(valor -> new PersonalIdentificador(valor.getIdentificador().toString()));
        var planoTreino = Optional.ofNullable(entidade.getPlanoTreino())
                .map(valor -> new PlanoDeTreinoIdentificador(valor.getIdentificador().toString()));
        var referencias = new ReferenciasRecomendacao(personal, planoTreino,
                Optional.ofNullable(entidade.getAula()).map(valor -> new AulaColetivaIdentificador(valor.toString())),
                Optional.ofNullable(entidade.getPlano()).map(valor -> new PlanoIdentificador(valor.toString())));
        Optional<SugestaoSubstituicao> sugestao = Optional.empty();
        var atual = entidade.getExercicioAtual();
        var sugerido = entidade.getExercicioSugerido();
        var series = entidade.getSeries();
        var minimas = entidade.getRepeticoesMinimas();
        var maximas = entidade.getRepeticoesMaximas();
        var carga = entidade.getCarga();
        var intervalo = entidade.getIntervaloSegundos();
        if (atual != null && sugerido != null && series != null && minimas != null && maximas != null && carga != null
                && intervalo != null) {
            sugestao = Optional.of(new SugestaoSubstituicao(new ExercicioIdentificador(atual.getIdentificador().toString()),
                    new ExercicioIdentificador(sugerido.getIdentificador().toString()), series, minimas, maximas, carga,
                    intervalo));
        }
        Optional<RespostaRecomendacao> resposta = Optional.empty();
        var instante = entidade.getResposta();
        var respondente = entidade.getRespondente();
        if (instante != null && respondente != null) {
            resposta = Optional.of(new RespostaRecomendacao(instante, respondente, Optional.ofNullable(entidade.getMotivo()),
                    Optional.ofNullable(entidade.getAjuste())));
        }
        var evidencias = new ArrayList<Evidencia>();
        for (var evidencia : entidade.getEvidencias()) {
            evidencias.add(new Evidencia(evidencia.getRotulo(), evidencia.getValor(), evidencia.isDestaque()));
        }
        return new Recomendacao(Optional.of(new RecomendacaoIdentificador(entidade.getIdentificador().toString())),
                new AlunoIdentificador(entidade.getAluno().getIdentificador().toString()), entidade.getCriterio(),
                entidade.getItem(), entidade.getTitulo(), entidade.getDescricao(), entidade.getJustificativa(), evidencias,
                referencias, sugestao, entidade.getGeracao(), entidade.getValidade(), entidade.getSituacao(), resposta,
                Optional.ofNullable(entidade.getPlanoGerado())
                        .map(valor -> new PlanoDeTreinoIdentificador(valor.getIdentificador().toString())),
                Optional.ofNullable(entidade.getCiencia()));
    }

    @Override
    public Recomendacao bloquear(RecomendacaoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        return paraAgregado(repositorioJpa.bloquear(paraIdentificadorJpa(identificador)).orElseThrow(
                () -> new NoSuchElementException(RecomendacaoServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador))));
    }

    @Override
    public List<Recomendacao> buscarPorAluno(AlunoIdentificador aluno) {
        Objects.requireNonNull(aluno);
        return repositorioJpa.buscarPorAluno(codigo(aluno)).stream().map(this::paraAgregado).toList();
    }

    @Override
    public List<RecomendacaoIdentificador> buscarVencidas(LocalDate hoje, int limite) {
        Objects.requireNonNull(hoje);
        return repositorioJpa.buscarVencidas(hoje, PageRequest.of(0, limite)).stream()
                .map(valor -> new RecomendacaoIdentificador(valor.toString())).toList();
    }
}
