package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao;

import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.AulaColetivaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

/** Registros a que a recomendação se refere: o personal e o plano de treino responsáveis, a aula ou o plano ofertado. */
public record ReferenciasRecomendacao(Optional<PersonalIdentificador> personal,
        Optional<PlanoDeTreinoIdentificador> planoTreino, Optional<AulaColetivaIdentificador> aula,
        Optional<PlanoIdentificador> plano) implements IObjetoValor {
    public ReferenciasRecomendacao {
        Objects.requireNonNull(personal);
        Objects.requireNonNull(planoTreino);
        Objects.requireNonNull(aula);
        Objects.requireNonNull(plano);
    }

    public static ReferenciasRecomendacao nenhuma() {
        return new ReferenciasRecomendacao(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static ReferenciasRecomendacao doPlanoDeTreino(PersonalIdentificador personal, PlanoDeTreinoIdentificador plano) {
        return new ReferenciasRecomendacao(Optional.of(personal), Optional.of(plano), Optional.empty(), Optional.empty());
    }

    public static ReferenciasRecomendacao daAula(AulaColetivaIdentificador aula) {
        return new ReferenciasRecomendacao(Optional.empty(), Optional.empty(), Optional.of(aula), Optional.empty());
    }

    public static ReferenciasRecomendacao doPlano(PlanoIdentificador plano) {
        return new ReferenciasRecomendacao(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(plano));
    }

    public static ReferenciasRecomendacao doPersonal(Optional<PersonalIdentificador> personal) {
        return new ReferenciasRecomendacao(personal, Optional.empty(), Optional.empty(), Optional.empty());
    }
}
