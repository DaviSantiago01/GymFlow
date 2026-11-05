package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.reserva;

import java.util.Optional;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;
public final class ReservaArmarioConsulta extends Consulta<ReservaArmarioOrdenacao> {
    public ReservaArmarioConsulta(Optional<ReservaArmarioOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina) {
        super(ordenacao,descendente,indicePagina,tamanhoPagina);
    }
}
