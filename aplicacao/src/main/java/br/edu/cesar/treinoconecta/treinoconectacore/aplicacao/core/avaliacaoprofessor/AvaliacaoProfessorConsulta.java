package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor;

import java.util.*;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;
public final class AvaliacaoProfessorConsulta extends Consulta<AvaliacaoProfessorOrdenacao> {
    private final Optional<String> busca;
    public AvaliacaoProfessorConsulta(Optional<AvaliacaoProfessorOrdenacao> ordenacao,Optional<Boolean> descendente,Optional<Integer> indicePagina,Optional<Integer> tamanhoPagina,Optional<String> busca) {
        super(ordenacao,descendente,indicePagina,tamanhoPagina);this.busca=Objects.requireNonNull(busca);
        Validate.isTrue(getTamanhoPagina()<=50,"Máximo de 50 registros por página");
        busca.ifPresent(b -> Validate.isTrue(b.length()<=100,"Busca muito longa"));
    }
    public Optional<String> getBusca() { return busca; }
}
