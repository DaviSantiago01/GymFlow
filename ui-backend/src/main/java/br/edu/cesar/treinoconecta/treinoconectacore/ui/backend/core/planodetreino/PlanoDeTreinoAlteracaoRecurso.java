package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.planodetreino;

import java.util.Objects;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoFormulario;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoAutorizacoes;
import dev.sauloaraujo.d5.framework.ui.AgregadoRecurso;

/** Resposta da rota de alteração: o plano, as autorizações e os dados do formulário. */
public class PlanoDeTreinoAlteracaoRecurso extends AgregadoRecurso<PlanoDeTreino, PlanoDeTreinoAutorizacoes> {
    private final PlanoDeTreinoFormulario formulario;

    public PlanoDeTreinoAlteracaoRecurso(PlanoDeTreino agregado, PlanoDeTreinoAutorizacoes autorizacoes,
            PlanoDeTreinoFormulario formulario) {
        super(agregado, autorizacoes);
        this.formulario = Objects.requireNonNull(formulario);
    }

    public PlanoDeTreinoFormulario getFormulario() {
        return formulario;
    }
}
