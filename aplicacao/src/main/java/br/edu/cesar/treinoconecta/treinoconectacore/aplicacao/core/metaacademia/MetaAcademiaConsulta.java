package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.metaacademia;

import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.SituacaoMetaAcademia;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class MetaAcademiaConsulta extends Consulta<MetaAcademiaOrdenacao> {
    private final Optional<SituacaoMetaAcademia> situacao;

    public MetaAcademiaConsulta(Optional<MetaAcademiaOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina, Optional<SituacaoMetaAcademia> situacao) {
        super(ordenacao, descendente, indicePagina, tamanhoPagina);
        this.situacao = Objects.requireNonNull(situacao);
    }

    public Optional<SituacaoMetaAcademia> getSituacao() { return situacao; }
}
