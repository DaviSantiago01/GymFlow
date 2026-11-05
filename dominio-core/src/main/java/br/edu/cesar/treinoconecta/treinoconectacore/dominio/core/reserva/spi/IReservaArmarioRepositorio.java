package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.spi;

import java.time.Instant;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.ArmarioCodigo;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;
public interface IReservaArmarioRepositorio extends IAgregadoRepositorio<ReservaArmarioIdentificador, ReservaArmario> {
    boolean existeAtivaPorAluno(AlunoIdentificador aluno, Instant agora);
    boolean existeSobreposicao(ArmarioCodigo armario, Instant inicio, Instant termino);
    ReservaArmario bloquear(ReservaArmarioIdentificador identificador);
    void vencerRelacionadas(AlunoIdentificador aluno, ArmarioCodigo armario, Instant agora);
    int vencerTodas(Instant agora);
}
