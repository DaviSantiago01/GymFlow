package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.meta;

import java.util.Objects;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.MetaFormulario;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.Meta;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.MetaAutorizacoes;
import dev.sauloaraujo.d5.framework.ui.AgregadoRecurso;

/** Resposta da rota de alteração: a meta, as autorizações e os dados do formulário. */
public class MetaAlteracaoRecurso extends AgregadoRecurso<Meta, MetaAutorizacoes> {
    private final MetaFormulario formulario;

    public MetaAlteracaoRecurso(Meta agregado, MetaAutorizacoes autorizacoes, MetaFormulario formulario) {
        super(agregado, autorizacoes);
        this.formulario = Objects.requireNonNull(formulario);
    }

    public MetaFormulario getFormulario() {
        return formulario;
    }
}
