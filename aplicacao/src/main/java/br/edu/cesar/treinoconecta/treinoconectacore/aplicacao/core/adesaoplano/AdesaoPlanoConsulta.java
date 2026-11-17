package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano;

import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.SituacaoAdesao;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class AdesaoPlanoConsulta extends Consulta<AdesaoPlanoOrdenacao> {
    private final Optional<String> aluno;
    private final Optional<SituacaoAdesao> situacao;

    public AdesaoPlanoConsulta(Optional<AdesaoPlanoOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina, Optional<String> aluno,
            Optional<SituacaoAdesao> situacao) {
        super(ordenacao, descendente, indicePagina, tamanhoPagina);
        this.aluno = Objects.requireNonNull(aluno);
        this.situacao = Objects.requireNonNull(situacao);
    }

    public Optional<String> getAluno() { return aluno; }
    public Optional<SituacaoAdesao> getSituacao() { return situacao; }
}
