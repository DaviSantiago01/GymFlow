package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.vinculopersonal;

import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.SituacaoVinculo;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class VinculoPersonalAlunoConsulta extends Consulta<VinculoPersonalAlunoOrdenacao> {
    private final Optional<String> profissional;
    private final Optional<String> aluno;
    private final Optional<SituacaoVinculo> situacao;

    public VinculoPersonalAlunoConsulta(Optional<VinculoPersonalAlunoOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina, Optional<String> profissional,
            Optional<String> aluno, Optional<SituacaoVinculo> situacao) {
        super(ordenacao, descendente, indicePagina, tamanhoPagina);
        this.profissional = Objects.requireNonNull(profissional);
        this.aluno = Objects.requireNonNull(aluno);
        this.situacao = Objects.requireNonNull(situacao);
    }

    public Optional<String> getProfissional() { return profissional; }
    public Optional<String> getAluno() { return aluno; }
    public Optional<SituacaoVinculo> getSituacao() { return situacao; }
}
