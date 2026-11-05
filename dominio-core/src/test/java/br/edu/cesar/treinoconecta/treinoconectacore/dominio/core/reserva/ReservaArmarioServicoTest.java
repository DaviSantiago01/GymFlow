package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva;

import java.time.*;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.spi.IArmarioRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.spi.IReservaArmarioRepositorio;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class ReservaArmarioServicoTest {
    @Test void bloqueiaAlunoEArmarioAntesDeVerificarDisponibilidade() {
        var seguranca=mock(ISegurancaServico.class);var alunos=mock(IAlunoRepositorio.class);
        var armarios=mock(IArmarioRepositorio.class);var reservas=mock(IReservaArmarioRepositorio.class);
        var agora=Instant.parse("2026-11-04T12:00:00Z");var aluno=new AlunoIdentificador("1");var armario=new ArmarioCodigo("A-01");
        when(seguranca.possuiAutorizacao("USUARIO")).thenReturn(true);when(seguranca.getUsuario()).thenReturn("u");
        when(alunos.bloquearPorUsuario("u")).thenReturn(new Aluno(Optional.of(aluno),"u","Aluno"));
        when(reservas.incluir(any())).thenAnswer(i->i.getArgument(0));
        var servico=new ReservaArmarioServico(seguranca,reservas,alunos,armarios,Clock.fixed(agora,ZoneOffset.UTC));
        assertEquals(agora.plusSeconds(10800),servico.reservar(armario).getTermino());
        var ordem=inOrder(alunos,armarios,reservas);
        ordem.verify(alunos).bloquearPorUsuario("u");ordem.verify(armarios).bloquear(armario);
        ordem.verify(reservas).vencerRelacionadas(aluno,armario,agora);
        ordem.verify(reservas).existeAtivaPorAluno(aluno,agora);ordem.verify(reservas).existeAtivaPorArmario(armario,agora);
        ordem.verify(reservas).incluir(any());
    }
}
