package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva;

import java.time.Instant;
import io.cucumber.java.pt.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.ArmarioCodigo;
import static org.junit.jupiter.api.Assertions.*;
public class ReservaArmarioStepDefinitions {
    private ReservaArmario reserva;
    private RuntimeException falha;
    private String indisponibilidade;
    @Dado("uma indisponibilidade por {string}") public void indisponivel(String motivo) { indisponibilidade=motivo; }
    @Quando("solicitar a reserva do armário disponível no catálogo") public void solicitar() {
        var seguranca=org.mockito.Mockito.mock(dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico.class);
        var alunos=org.mockito.Mockito.mock(br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio.class);
        var armarios=org.mockito.Mockito.mock(br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.spi.IArmarioRepositorio.class);
        var reservas=org.mockito.Mockito.mock(br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.spi.IReservaArmarioRepositorio.class);
        var agora=Instant.parse("2026-11-04T12:00:00Z");
        var aluno=new AlunoIdentificador("1");var armario=new ArmarioCodigo("A-01");
        org.mockito.Mockito.when(seguranca.possuiAutorizacao("USUARIO")).thenReturn(true);
        org.mockito.Mockito.when(seguranca.getUsuario()).thenReturn("u");
        org.mockito.Mockito.when(alunos.bloquearPorUsuario("u")).thenReturn(new br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.Aluno(java.util.Optional.of(aluno),"u","Aluno"));
        org.mockito.Mockito.when(reservas.existeAtivaPorAluno(aluno,agora)).thenReturn(indisponibilidade.equals("aluno"));
        org.mockito.Mockito.when(reservas.existeSobreposicao(armario,agora,agora.plusSeconds(10800))).thenReturn(indisponibilidade.equals("armario"));
        var servico=new ReservaArmarioServico(seguranca,reservas,alunos,armarios,java.time.Clock.fixed(agora,java.time.ZoneOffset.UTC));
        try { servico.reservar(armario, java.time.LocalDate.parse("2026-11-04"), 9, 3); } catch(RuntimeException e) { falha=e; }
        org.mockito.Mockito.verify(reservas,org.mockito.Mockito.never()).incluir(org.mockito.ArgumentMatchers.any());
    }
    @Dado("uma reserva confirmada às {string}") public void confirmar(String horario) {
        reserva=ReservaArmario.confirmar(new AlunoIdentificador("1"),new ArmarioCodigo("A-01"),Instant.parse(horario),new PeriodoReserva(Instant.parse(horario),Instant.parse(horario).plusSeconds(10800)));
    }
    @Então("o término previsto é {string}") public void termino(String horario) { assertEquals(Instant.parse(horario),reserva.getTermino()); }
    @Então("a reserva está ativa às {string}") public void ativa(String horario) { assertTrue(reserva.ativaEm(Instant.parse(horario))); }
    @Quando("processar o vencimento às {string}") public void vencer(String horario) { reserva=reserva.vencer(Instant.parse(horario)); }
    @Então("a situação é {string}") public void situacao(String valor) { assertEquals(SituacaoReserva.valueOf(valor),reserva.getSituacao()); }
    @Então("o encerramento real é {string}") public void encerramento(String horario) { assertEquals(Instant.parse(horario),reserva.getEncerramento().orElseThrow()); }
    @Quando("o aluno {string} solicitar {string} às {string}") public void finalizar(String aluno,String acao,String horario) {
        try { reserva=acao.equals("cancelar")?reserva.cancelar(new AlunoIdentificador(aluno),Instant.parse(horario)):reserva.encerrar(new AlunoIdentificador(aluno),Instant.parse(horario)); }
        catch(RuntimeException e) { falha=e; }
    }
    @Então("a operação é rejeitada") public void rejeitada() { assertNotNull(falha); }
    @Quando("agendar em {string} às {int} por {int} horas") public void agendar(String data,int hora,int duracao) {
        try {
            reserva=ReservaArmario.confirmar(new AlunoIdentificador("1"),new ArmarioCodigo("A-01"),
                Instant.parse("2026-11-04T12:00:00Z"),PeriodoReserva.criar(java.time.LocalDate.parse(data),hora,duracao));
        } catch(RuntimeException e) { falha=e; }
    }
}
