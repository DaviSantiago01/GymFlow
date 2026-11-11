package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.avaliacaofisica;

import java.util.Objects;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.AvaliacaoFisicaFormulario;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisica;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaAutorizacoes;
import dev.sauloaraujo.d5.framework.ui.AgregadoRecurso;

/** Resposta da rota de alteração: a avaliação, as autorizações e os dados do formulário. */
public class AvaliacaoFisicaAlteracaoRecurso extends AgregadoRecurso<AvaliacaoFisica, AvaliacaoFisicaAutorizacoes> {
    private final AvaliacaoFisicaFormulario formulario;

    public AvaliacaoFisicaAlteracaoRecurso(AvaliacaoFisica agregado, AvaliacaoFisicaAutorizacoes autorizacoes,
            AvaliacaoFisicaFormulario formulario) {
        super(agregado, autorizacoes);
        this.formulario = Objects.requireNonNull(formulario);
    }

    public AvaliacaoFisicaFormulario getFormulario() {
        return formulario;
    }
}
