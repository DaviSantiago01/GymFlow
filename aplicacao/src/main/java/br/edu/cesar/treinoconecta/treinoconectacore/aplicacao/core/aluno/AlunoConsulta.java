package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno;

import java.util.Objects;
import java.util.Optional;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class AlunoConsulta extends Consulta<AlunoOrdenacao> {
    private final Optional<String> nome;

    public AlunoConsulta(Optional<AlunoOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina, Optional<String> nome) {
        super(ordenacao, descendente, indicePagina, tamanhoPagina);
        this.nome = Objects.requireNonNull(nome);
    }

    public Optional<String> getNome() { return nome; }
}
