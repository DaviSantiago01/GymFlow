package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.profissional;

import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ClassificacaoProfissional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.SituacaoProfissional;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

/** A busca procura o trecho no nome, no CPF ou no CREF. */
public final class ProfissionalConsulta extends Consulta<ProfissionalOrdenacao> {
    private final Optional<String> busca;
    private final Optional<ClassificacaoProfissional> classificacao;
    private final Optional<SituacaoProfissional> situacao;

    public ProfissionalConsulta(Optional<ProfissionalOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina, Optional<String> busca,
            Optional<ClassificacaoProfissional> classificacao, Optional<SituacaoProfissional> situacao) {
        super(ordenacao, descendente, indicePagina, tamanhoPagina);
        this.busca = Objects.requireNonNull(busca);
        this.classificacao = Objects.requireNonNull(classificacao);
        this.situacao = Objects.requireNonNull(situacao);
    }

    public Optional<String> getBusca() { return busca; }
    public Optional<ClassificacaoProfissional> getClassificacao() { return classificacao; }
    public Optional<SituacaoProfissional> getSituacao() { return situacao; }
}
