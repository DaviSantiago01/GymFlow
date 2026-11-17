package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade;

import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.SituacaoMensalidade;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class MensalidadeConsulta extends Consulta<MensalidadeOrdenacao> {
    private final Optional<String> aluno;
    private final Optional<String> adesao;
    private final Optional<SituacaoMensalidade> situacao;

    public MensalidadeConsulta(Optional<MensalidadeOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina, Optional<String> aluno, Optional<String> adesao,
            Optional<SituacaoMensalidade> situacao) {
        super(ordenacao, descendente, indicePagina, tamanhoPagina);
        this.aluno = Objects.requireNonNull(aluno);
        this.adesao = Objects.requireNonNull(adesao);
        this.situacao = Objects.requireNonNull(situacao);
    }

    public Optional<String> getAluno() { return aluno; }
    public Optional<String> getAdesao() { return adesao; }
    public Optional<SituacaoMensalidade> getSituacao() { return situacao; }
}
