package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao;

import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.CategoriaRecomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.SituacaoRecomendacao;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class RecomendacaoConsulta extends Consulta<RecomendacaoOrdenacao> {
    private final Optional<String> aluno;
    private final Optional<SituacaoRecomendacao> situacao;
    private final Optional<CategoriaRecomendacao> categoria;
    private final Optional<Boolean> sugestoesDeTreino;
    private final Optional<Integer> dias;

    public RecomendacaoConsulta(Optional<RecomendacaoOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina, Optional<String> aluno,
            Optional<SituacaoRecomendacao> situacao, Optional<CategoriaRecomendacao> categoria,
            Optional<Boolean> sugestoesDeTreino, Optional<Integer> dias) {
        super(ordenacao, descendente, indicePagina, tamanhoPagina);
        this.aluno = Objects.requireNonNull(aluno).filter(valor -> !valor.isBlank());
        this.situacao = Objects.requireNonNull(situacao);
        this.categoria = Objects.requireNonNull(categoria);
        this.sugestoesDeTreino = Objects.requireNonNull(sugestoesDeTreino);
        this.dias = Objects.requireNonNull(dias);
        dias.ifPresent(valor -> Validate.isTrue(valor > 0 && valor <= 3660, "Quantidade de dias inválida"));
    }

    public Optional<String> getAluno() { return aluno; }
    public Optional<SituacaoRecomendacao> getSituacao() { return situacao; }
    public Optional<CategoriaRecomendacao> getCategoria() { return categoria; }

    /** Para o personal: somente as sugestões de treino enviadas para ele analisar. */
    public Optional<Boolean> getSugestoesDeTreino() { return sugestoesDeTreino; }

    public Optional<Integer> getDias() { return dias; }
}
