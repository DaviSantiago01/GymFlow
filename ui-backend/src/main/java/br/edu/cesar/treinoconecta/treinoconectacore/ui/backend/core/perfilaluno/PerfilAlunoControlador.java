package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.perfilaluno;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.NivelExperiencia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.ObjetivoAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.PerfilAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.PerfilAlunoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.RegiaoCorporal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.RestricaoFisica;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.CategoriaRecomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.ParametrosRecomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.RecomendacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.ResumoAluno;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "core/perfil-aluno")
public class PerfilAlunoControlador {
    private final PerfilAlunoServico servico;
    private final RecomendacaoServico recomendacoes;

    public PerfilAlunoControlador(PerfilAlunoServico servico, RecomendacaoServico recomendacoes) {
        this.servico = Objects.requireNonNull(servico);
        this.recomendacoes = Objects.requireNonNull(recomendacoes);
    }

    public record Opcao(String valor, String descricao) {
    }

    public record CategoriaOpcao(CategoriaRecomendacao categoria, String descricao, int prioridade, boolean opcional) {
    }

    public record PerfilVisualizacao(String aluno, Optional<ObjetivoAluno> objetivo, Optional<NivelExperiencia> nivel,
            List<DiaSemana> dias, Optional<LocalTime> inicio, Optional<LocalTime> fim, List<RestricaoFisica> restricoes,
            List<CategoriaRecomendacao> categoriasDesativadas, boolean completo, List<String> pendentes,
            Instant atualizacao, ResumoAluno resumo, List<Opcao> objetivos, List<Opcao> niveis, List<Opcao> regioes,
            List<CategoriaOpcao> categorias, int maximoExibidas, int carenciaRecusaDias) {
    }

    public record DadosTreinoRequisicao(@Nullable String objetivo, @Nullable String nivel, @Nullable List<String> dias,
            @Nullable LocalTime inicio, @Nullable LocalTime fim) {
    }

    public record RestricaoRequisicao(@Nullable String regiao, @Nullable String descricao) {
    }

    public record PreferenciasRequisicao(@Nullable List<String> categoriasDesativadas) {
    }

    @GetMapping("meu")
    public PerfilVisualizacao meu() {
        return visualizar(servico.meuPerfil(), recomendacoes.meuResumo());
    }

    @GetMapping("aluno/{aluno}")
    public PerfilVisualizacao doAluno(@PathVariable AlunoIdentificador aluno) {
        return visualizar(servico.consultar(aluno), recomendacoes.resumo(aluno));
    }

    @PostMapping("dados-treino")
    public PerfilVisualizacao salvarDadosDeTreino(@RequestBody DadosTreinoRequisicao requisicao) {
        var perfil = servico.salvarDadosDeTreino(texto(requisicao.objetivo()), texto(requisicao.nivel()),
                requisicao.dias() == null ? List.of() : requisicao.dias(), requisicao.inicio(), requisicao.fim());
        return visualizar(perfil, recomendacoes.meuResumo());
    }

    @PostMapping("restricoes")
    public PerfilVisualizacao registrarRestricao(@RequestBody RestricaoRequisicao requisicao) {
        var perfil = servico.registrarRestricao(texto(requisicao.regiao()), texto(requisicao.descricao()));
        return visualizar(perfil, recomendacoes.meuResumo());
    }

    @PostMapping("restricoes/{regiao}/remover")
    public PerfilVisualizacao removerRestricao(@PathVariable String regiao) {
        return visualizar(servico.removerRestricao(regiao), recomendacoes.meuResumo());
    }

    @PostMapping("preferencias")
    public PerfilVisualizacao salvarPreferencias(@RequestBody PreferenciasRequisicao requisicao) {
        var perfil = servico.salvarPreferencias(
                requisicao.categoriasDesativadas() == null ? List.of() : requisicao.categoriasDesativadas());
        return visualizar(perfil, recomendacoes.meuResumo());
    }

    private static String texto(@Nullable String valor) {
        return valor == null ? "" : valor;
    }

    private static PerfilVisualizacao visualizar(PerfilAluno perfil, ResumoAluno resumo) {
        var disponibilidade = perfil.getDisponibilidade();
        return new PerfilVisualizacao(perfil.getAluno().toString(), perfil.getObjetivo(), perfil.getNivel(),
                disponibilidade.map(valor -> valor.dias().stream().sorted().toList()).orElse(List.of()),
                disponibilidade.map(valor -> valor.inicio()), disponibilidade.map(valor -> valor.fim()),
                perfil.getRestricoes(), perfil.getCategoriasDesativadas().stream().sorted().toList(), perfil.completo(),
                perfil.camposPendentes(), perfil.getAtualizacao(), resumo,
                Arrays.stream(ObjetivoAluno.values()).map(valor -> new Opcao(valor.name(), valor.getDescricao())).toList(),
                Arrays.stream(NivelExperiencia.values()).map(valor -> new Opcao(valor.name(), valor.getDescricao())).toList(),
                Arrays.stream(RegiaoCorporal.values()).map(valor -> new Opcao(valor.name(), valor.getDescricao())).toList(),
                Arrays.stream(CategoriaRecomendacao.values()).map(valor -> new CategoriaOpcao(valor, valor.getDescricao(),
                        valor.getPrioridade(), valor.isOpcional())).toList(),
                ParametrosRecomendacao.MAXIMO_EXIBIDAS, ParametrosRecomendacao.CARENCIA_RECUSA_DIAS);
    }
}
