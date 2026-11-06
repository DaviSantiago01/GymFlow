package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.professor;

import java.util.*;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;
public final class ProfessorConsulta extends Consulta<ProfessorOrdenacao> {
    private final Optional<String> busca;
    public ProfessorConsulta(Optional<ProfessorOrdenacao> ordenacao,Optional<Boolean> descendente,Optional<Integer> indicePagina,Optional<Integer> tamanhoPagina,Optional<String> busca) {
        super(ordenacao,descendente,indicePagina,tamanhoPagina);this.busca=Objects.requireNonNull(busca);
        Validate.isTrue(getTamanhoPagina()<=50,"Máximo de 50 registros por página");
        busca.ifPresent(b -> Validate.isTrue(b.length()<=100,"Busca muito longa"));
    }
    public Optional<String> getBusca() { return busca; }
}
