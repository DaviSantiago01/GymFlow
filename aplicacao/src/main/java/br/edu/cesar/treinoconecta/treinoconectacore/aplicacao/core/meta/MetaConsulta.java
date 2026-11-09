package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta;

import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.SituacaoMeta;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class MetaConsulta extends Consulta<MetaOrdenacao> {
    private final Optional<String> aluno;
    private final Optional<SituacaoMeta> situacao;

    public MetaConsulta(Optional<MetaOrdenacao> ordenacao, Optional<Boolean> descendente, Optional<Integer> indicePagina,
            Optional<Integer> tamanhoPagina, Optional<String> aluno, Optional<SituacaoMeta> situacao) {
        super(ordenacao, descendente, indicePagina, tamanhoPagina);
        this.aluno = Objects.requireNonNull(aluno);
        this.situacao = Objects.requireNonNull(situacao);
    }

    public Optional<String> getAluno() { return aluno; }
    public Optional<SituacaoMeta> getSituacao() { return situacao; }
}
