package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.CategoriaRecomendacao;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

/**
 * Dados que o aluno informa para receber recomendações personalizadas: objetivo, nível de experiência, disponibilidade
 * semanal, restrições físicas e as categorias opcionais de recomendação que ele não quer receber.
 */
public final class PerfilAluno implements IAgregado<PerfilAlunoIdentificador> {
    private final AlunoIdentificador aluno;
    private final Optional<ObjetivoAluno> objetivo;
    private final Optional<NivelExperiencia> nivel;
    private final Optional<Disponibilidade> disponibilidade;
    private final List<RestricaoFisica> restricoes;
    private final Set<CategoriaRecomendacao> categoriasDesativadas;
    private final Instant atualizacao;

    public PerfilAluno(AlunoIdentificador aluno, Optional<ObjetivoAluno> objetivo, Optional<NivelExperiencia> nivel,
            Optional<Disponibilidade> disponibilidade, List<RestricaoFisica> restricoes,
            Set<CategoriaRecomendacao> categoriasDesativadas, Instant atualizacao) {
        this.aluno = Objects.requireNonNull(aluno);
        this.objetivo = Objects.requireNonNull(objetivo);
        this.nivel = Objects.requireNonNull(nivel);
        this.disponibilidade = Objects.requireNonNull(disponibilidade);
        this.restricoes = List.copyOf(restricoes);
        var regioes = EnumSet.noneOf(RegiaoCorporal.class);
        for (var restricao : this.restricoes) {
            Validate.isTrue(regioes.add(restricao.regiao()), PerfilAlunoServico.RESTRICAO_REPETIDA_MODELO,
                    restricao.regiao().getDescricao());
        }
        this.categoriasDesativadas = categoriasDesativadas.isEmpty() ? Set.of()
                : Set.copyOf(EnumSet.copyOf(categoriasDesativadas));
        for (var categoria : this.categoriasDesativadas) {
            Validate.isTrue(categoria.isOpcional(), PerfilAlunoServico.CATEGORIA_OBRIGATORIA_MODELO,
                    categoria.getDescricao());
        }
        this.atualizacao = Objects.requireNonNull(atualizacao);
    }

    public static PerfilAluno vazio(AlunoIdentificador aluno, Instant agora) {
        return new PerfilAluno(aluno, Optional.empty(), Optional.empty(), Optional.empty(), List.of(), Set.of(), agora);
    }

    public PerfilAluno atualizarDadosDeTreino(ObjetivoAluno novoObjetivo, NivelExperiencia novoNivel,
            Disponibilidade novaDisponibilidade, Instant agora) {
        Validate.notNull(novoObjetivo, PerfilAlunoServico.OBJETIVO_INVALIDO);
        Validate.notNull(novoNivel, PerfilAlunoServico.NIVEL_INVALIDO);
        Validate.notNull(novaDisponibilidade, PerfilAlunoServico.DISPONIBILIDADE_INVALIDA);
        return new PerfilAluno(aluno, Optional.of(novoObjetivo), Optional.of(novoNivel), Optional.of(novaDisponibilidade),
                restricoes, categoriasDesativadas, agora);
    }

    /** Inclui a restrição ou, se já houver uma na mesma região, atualiza a descrição. */
    public PerfilAluno registrarRestricao(RegiaoCorporal regiao, String descricao, LocalDate hoje, Instant agora) {
        var nova = new RestricaoFisica(regiao, descricao, hoje);
        var lista = new ArrayList<RestricaoFisica>();
        boolean substituiu = false;
        for (var restricao : restricoes) {
            if (restricao.regiao() == regiao) {
                lista.add(nova);
                substituiu = true;
            } else {
                lista.add(restricao);
            }
        }
        if (!substituiu) {
            lista.add(nova);
        }
        return new PerfilAluno(aluno, objetivo, nivel, disponibilidade, lista, categoriasDesativadas, agora);
    }

    public PerfilAluno removerRestricao(RegiaoCorporal regiao, Instant agora) {
        Objects.requireNonNull(regiao);
        var lista = restricoes.stream().filter(restricao -> restricao.regiao() != regiao).toList();
        Validate.validState(lista.size() < restricoes.size(), PerfilAlunoServico.RESTRICAO_INEXISTENTE);
        return new PerfilAluno(aluno, objetivo, nivel, disponibilidade, lista, categoriasDesativadas, agora);
    }

    /** RN017: o aluno pode desativar categorias opcionais; o acompanhamento físico não pode ser desativado. */
    public PerfilAluno definirPreferencias(Set<CategoriaRecomendacao> desativadas, Instant agora) {
        Objects.requireNonNull(desativadas);
        return new PerfilAluno(aluno, objetivo, nivel, disponibilidade, restricoes, desativadas, agora);
    }

    /** RN002: objetivo, nível e disponibilidade são obrigatórios para recomendações personalizadas. */
    public boolean completo() {
        return objetivo.isPresent() && nivel.isPresent() && disponibilidade.isPresent();
    }

    public List<String> camposPendentes() {
        var pendentes = new ArrayList<String>();
        if (objetivo.isEmpty()) {
            pendentes.add("objetivo");
        }
        if (nivel.isEmpty()) {
            pendentes.add("nível de experiência");
        }
        if (disponibilidade.isEmpty()) {
            pendentes.add("disponibilidade semanal");
        }
        return pendentes;
    }

    public boolean aceita(CategoriaRecomendacao categoria) {
        return !categoriasDesativadas.contains(categoria);
    }

    /** RN004: primeira restrição do aluno em uma das regiões contraindicadas, se houver. */
    public Optional<RestricaoFisica> restricaoConflitante(Set<RegiaoCorporal> contraindicacoes) {
        Objects.requireNonNull(contraindicacoes);
        return restricoes.stream().filter(restricao -> contraindicacoes.contains(restricao.regiao())).findFirst();
    }

    @Override
    public Optional<PerfilAlunoIdentificador> getIdentificador() {
        return Optional.of(new PerfilAlunoIdentificador(aluno.toString()));
    }

    public AlunoIdentificador getAluno() { return aluno; }
    public Optional<ObjetivoAluno> getObjetivo() { return objetivo; }
    public Optional<NivelExperiencia> getNivel() { return nivel; }
    public Optional<Disponibilidade> getDisponibilidade() { return disponibilidade; }
    public List<RestricaoFisica> getRestricoes() { return restricoes; }
    public Set<CategoriaRecomendacao> getCategoriasDesativadas() { return categoriasDesativadas; }
    public Instant getAtualizacao() { return atualizacao; }
}
