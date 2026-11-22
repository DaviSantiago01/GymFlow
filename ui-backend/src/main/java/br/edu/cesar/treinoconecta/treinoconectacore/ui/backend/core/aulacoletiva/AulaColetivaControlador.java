package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.aulacoletiva;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.RecomendacaoAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.RecomendacaoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.RecomendacaoOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.AulaColetiva;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.AulaColetivaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.AulaColetivaServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.ImpactoAula;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.PerfilAlunoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.CategoriaRecomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.SituacaoRecomendacao;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "core/aula-coletiva")
public class AulaColetivaControlador {
    private static final BigDecimal CEM = new BigDecimal("100");

    private final AulaColetivaServico servico;
    private final PerfilAlunoServico perfis;
    private final RecomendacaoAplicacaoServico recomendacoes;

    public AulaColetivaControlador(AulaColetivaServico servico, PerfilAlunoServico perfis,
            RecomendacaoAplicacaoServico recomendacoes) {
        this.servico = Objects.requireNonNull(servico);
        this.perfis = Objects.requireNonNull(perfis);
        this.recomendacoes = Objects.requireNonNull(recomendacoes);
    }

    public record AulaResumo(String identificador, String nome, String modalidade, String instrutor, String sala,
            DiaSemana dia, LocalTime inicio, LocalTime fim, int capacidade, int inscritos, int vagas, BigDecimal ocupacao,
            String nivel, String impacto, boolean baixoImpacto, boolean inscrito, Optional<String> recomendacao,
            Optional<String> motivo) {
    }

    public record AulasDoAluno(boolean perfilCompleto, List<String> pendentes, List<AulaResumo> recomendadas,
            List<AulaResumo> indisponiveis, List<AulaResumo> inscricoes, List<AulaResumo> todas) {
    }

    /** RN004 a RN006: aulas compatíveis com o aluno e, para as demais, o motivo de não aparecerem. */
    @GetMapping("para-voce")
    public AulasDoAluno paraVoce() {
        var perfil = perfis.meuPerfil();
        var inscricoes = servico.minhasInscricoes();
        var consulta = new RecomendacaoConsulta(Optional.of(RecomendacaoOrdenacao.GERACAO), Optional.of(true),
                Optional.empty(), Optional.of(Consulta.TAMANHO_PAGINA_MAXIMO), Optional.empty(),
                Optional.of(SituacaoRecomendacao.PENDENTE), Optional.of(CategoriaRecomendacao.AULAS_COLETIVAS),
                Optional.empty(), Optional.empty());
        var recomendacaoPorAula = new HashMap<String, String>();
        for (var recomendacao : recomendacoes.pesquisar(consulta).getProjecoes()) {
            recomendacao.aula().ifPresent(aula -> recomendacaoPorAula.putIfAbsent(aula, recomendacao.identificador()));
        }
        var aulas = servico.listar();
        var recomendadas = new ArrayList<AulaResumo>();
        var indisponiveis = new ArrayList<AulaResumo>();
        var todas = new ArrayList<AulaResumo>();
        for (var aula : aulas) {
            var codigo = aula.getIdentificador().orElseThrow().toString();
            var motivo = perfil.completo() ? aula.incompatibilidadeCom(perfil, inscricoes)
                    : Optional.<AulaColetiva.MotivoIncompatibilidade>empty();
            var resumo = resumir(aula, aula.inscrito(perfil.getAluno()), Optional.ofNullable(recomendacaoPorAula.get(codigo)),
                    motivo.map(AulaColetiva.MotivoIncompatibilidade::descricao));
            todas.add(resumo);
            if (!perfil.completo() || resumo.inscrito()) {
                continue;
            }
            (motivo.isEmpty() ? recomendadas : indisponiveis).add(resumo);
        }
        var minhas = inscricoes.stream().map(aula -> resumir(aula, true, Optional.empty(), Optional.empty())).toList();
        return new AulasDoAluno(perfil.completo(), perfil.camposPendentes(), recomendadas, indisponiveis, minhas, todas);
    }

    @PostMapping("{identificador}/inscrever")
    public AulaResumo inscrever(@PathVariable AulaColetivaIdentificador identificador) {
        return resumir(servico.inscrever(identificador), true, Optional.empty(), Optional.empty());
    }

    private static AulaResumo resumir(AulaColetiva aula, boolean inscrito, Optional<String> recomendacao,
            Optional<String> motivo) {
        var ocupacao = BigDecimal.valueOf(aula.getInscritos().size()).multiply(CEM)
                .divide(BigDecimal.valueOf(aula.getCapacidade()), 0, RoundingMode.HALF_UP);
        return new AulaResumo(aula.getIdentificador().orElseThrow().toString(), aula.getNome(), aula.getModalidade(),
                aula.getInstrutor(), aula.getSala(), aula.getDia(), aula.getInicio(), aula.getFim(), aula.getCapacidade(),
                aula.getInscritos().size(), aula.vagas(), ocupacao, aula.getNivel().getDescricao(),
                aula.getImpacto().getDescricao(), aula.getImpacto() == ImpactoAula.BAIXO,
                inscrito, recomendacao, motivo);
    }
}
