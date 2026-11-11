package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino;

import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.SituacaoPlano;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class PlanoDeTreinoConsulta extends Consulta<PlanoDeTreinoOrdenacao> {
    private final Optional<String> aluno;
    private final Optional<SituacaoPlano> situacao;

    public PlanoDeTreinoConsulta(Optional<PlanoDeTreinoOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina, Optional<String> aluno,
            Optional<SituacaoPlano> situacao) {
        super(ordenacao, descendente, indicePagina, tamanhoPagina);
        this.aluno = Objects.requireNonNull(aluno);
        this.situacao = Objects.requireNonNull(situacao);
    }

    public Optional<String> getAluno() { return aluno; }
    public Optional<SituacaoPlano> getSituacao() { return situacao; }
}
