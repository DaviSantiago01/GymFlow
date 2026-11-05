package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.armario;

import java.util.Optional;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;
public final class ArmarioConsulta extends Consulta<ArmarioOrdenacao> {
    public ArmarioConsulta(Optional<ArmarioOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina) {
        super(ordenacao,descendente,indicePagina,tamanhoPagina);
    }
}
