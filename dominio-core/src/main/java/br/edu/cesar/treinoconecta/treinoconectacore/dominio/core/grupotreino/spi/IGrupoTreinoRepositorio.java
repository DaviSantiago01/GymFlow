package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.spi;

import java.time.Instant;
import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.academia.AcademiaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;
public interface IGrupoTreinoRepositorio extends IAgregadoRepositorio<GrupoTreinoIdentificador,GrupoTreino> {
    GrupoTreino bloquear(GrupoTreinoIdentificador id);
    List<GrupoTreinoIdentificador> vencidos(Instant agora,int limite);
    /** Serializa as propostas de treino da academia, para que duas propostas simultâneas não ocupem a mesma vaga. */
    void bloquearAgenda(AcademiaIdentificador academia);
    /** Treinos de grupo não cancelados da academia que se sobrepõem ao intervalo [inicio, fim). */
    int contarTreinosNoHorario(AcademiaIdentificador academia,Instant inicio,Instant fim);
    /** Indica se o professor já conduz um treino de grupo não cancelado que se sobrepõe ao intervalo [inicio, fim). */
    boolean professorOcupado(ProfissionalIdentificador professor,Instant inicio,Instant fim);
}
