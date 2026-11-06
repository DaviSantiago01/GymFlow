package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.spi;

import java.time.Instant;
import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.*;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;
public interface IGrupoTreinoRepositorio extends IAgregadoRepositorio<GrupoTreinoIdentificador,GrupoTreino> {
    GrupoTreino bloquear(GrupoTreinoIdentificador id);
    List<GrupoTreinoIdentificador> vencidos(Instant agora,int limite);
}
