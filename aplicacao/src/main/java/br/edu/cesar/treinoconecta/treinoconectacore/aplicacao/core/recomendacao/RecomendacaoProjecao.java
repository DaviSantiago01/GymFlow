package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.CategoriaRecomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.CriterioRecomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.Recomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.SituacaoRecomendacao;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

/** Recomendação com os nomes do aluno, do personal, do plano de treino, dos exercícios e da aula referenciados. */
public record RecomendacaoProjecao(String identificador, String aluno, String alunoNome, CategoriaRecomendacao categoria,
        CriterioRecomendacao criterio, String titulo, String descricao, String justificativa,
        SituacaoRecomendacao situacao, Instant geracao, LocalDate validade, Optional<Instant> resposta,
        Optional<String> respondente, Optional<String> motivo, Optional<String> ajuste, Optional<String> personal,
        Optional<String> personalNome, Optional<String> planoTreino, Optional<String> planoTreinoNome,
        Optional<Integer> planoTreinoVersao, Optional<String> exercicioAtual, Optional<String> exercicioSugerido,
        Optional<String> aula, Optional<String> planoOfertado, Optional<String> planoGerado,
        Optional<Integer> planoGeradoVersao, Optional<Instant> ciencia) implements IProjecao<Recomendacao> {
    public RecomendacaoProjecao(Long identificador, Long aluno, String alunoNome, CategoriaRecomendacao categoria,
            CriterioRecomendacao criterio, String titulo, String descricao, String justificativa,
            SituacaoRecomendacao situacao, Instant geracao, LocalDate validade, @Nullable Instant resposta,
            @Nullable String respondente, @Nullable String motivo, @Nullable String ajuste, @Nullable Long personal,
            @Nullable String personalNome, @Nullable Long planoTreino, @Nullable String planoTreinoNome,
            @Nullable Integer planoTreinoVersao, @Nullable String exercicioAtual, @Nullable String exercicioSugerido,
            @Nullable Long aula, @Nullable Long planoOfertado, @Nullable Long planoGerado,
            @Nullable Integer planoGeradoVersao, @Nullable Instant ciencia) {
        this(identificador.toString(), aluno.toString(), alunoNome, categoria, criterio, titulo, descricao, justificativa,
                situacao, geracao, validade, Optional.ofNullable(resposta), Optional.ofNullable(respondente),
                Optional.ofNullable(motivo), Optional.ofNullable(ajuste), texto(personal), Optional.ofNullable(personalNome),
                texto(planoTreino), Optional.ofNullable(planoTreinoNome), Optional.ofNullable(planoTreinoVersao),
                Optional.ofNullable(exercicioAtual), Optional.ofNullable(exercicioSugerido), texto(aula),
                texto(planoOfertado), texto(planoGerado), Optional.ofNullable(planoGeradoVersao),
                Optional.ofNullable(ciencia));
    }

    private static Optional<String> texto(@Nullable Long valor) {
        return Optional.ofNullable(valor).map(Object::toString);
    }
}
