package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano;

import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.SituacaoPlano;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class PlanoConsulta extends Consulta<PlanoOrdenacao> {
    private final Optional<String> nome;
    private final Optional<SituacaoPlano> situacao;

    public PlanoConsulta(Optional<PlanoOrdenacao> ordenacao, Optional<Boolean> descendente, Optional<Integer> indicePagina,
            Optional<Integer> tamanhoPagina, Optional<String> nome, Optional<SituacaoPlano> situacao) {
        super(ordenacao, descendente, indicePagina, tamanhoPagina);
        this.nome = Objects.requireNonNull(nome);
        this.situacao = Objects.requireNonNull(situacao);
    }

    public Optional<String> getNome() { return nome; }
    public Optional<SituacaoPlano> getSituacao() { return situacao; }
}
