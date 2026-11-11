package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica;

import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.SituacaoAvaliacao;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class AvaliacaoFisicaConsulta extends Consulta<AvaliacaoFisicaOrdenacao> {
    private final Optional<String> aluno;
    private final Optional<SituacaoAvaliacao> situacao;

    public AvaliacaoFisicaConsulta(Optional<AvaliacaoFisicaOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina, Optional<String> aluno,
            Optional<SituacaoAvaliacao> situacao) {
        super(ordenacao, descendente, indicePagina, tamanhoPagina);
        this.aluno = Objects.requireNonNull(aluno);
        this.situacao = Objects.requireNonNull(situacao);
    }

    public Optional<String> getAluno() { return aluno; }
    public Optional<SituacaoAvaliacao> getSituacao() { return situacao; }
}
