package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.grupotreino;

import java.util.*;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;
public final class GrupoTreinoConsulta extends Consulta<GrupoTreinoOrdenacao> {
    public GrupoTreinoConsulta(Optional<GrupoTreinoOrdenacao> ordenacao,Optional<Boolean> descendente,Optional<Integer> indicePagina,Optional<Integer> tamanhoPagina){super(ordenacao,descendente,indicePagina,tamanhoPagina);org.apache.commons.lang3.Validate.isTrue(getTamanhoPagina()<=50,"Máximo de 50 registros por página");}
}
