package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino;
import java.time.*;
import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.academia.AcademiaIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;
import io.cucumber.java.pt.*;
import static org.junit.jupiter.api.Assertions.*;
public class GrupoTreinoStepDefinitions {
 final Instant agora=Instant.parse("2026-11-06T12:00:00Z");final AlunoIdentificador organizador=new AlunoIdentificador("1"),membro=new AlunoIdentificador("2");GrupoTreino grupo;RuntimeException falha;
 @Dado("um grupo ativo com organizador e outro membro")public void preparar(){grupo=new GrupoTreino(Optional.of(new GrupoTreinoIdentificador("1")),new NomeGrupo("Grupo"),organizador,new AcademiaIdentificador("1"),agora,Optional.empty(),List.of(new ParticipacaoGrupo(Optional.of(1L),organizador,agora,Optional.empty()),new ParticipacaoGrupo(Optional.of(2L),membro,agora,Optional.empty())),List.of(),List.of(new TreinoGrupo(Optional.of(1L),agora.plusSeconds(14400),agora.plusSeconds(7200),agora,Optional.empty(),List.of(new ConviteTreino(Optional.of(1L),organizador,SituacaoConviteTreino.PENDENTE,agora,Optional.empty(),Optional.empty(),Optional.empty()),new ConviteTreino(Optional.of(2L),membro,SituacaoConviteTreino.PENDENTE,agora,Optional.empty(),Optional.empty(),Optional.empty())))));}
 @Quando("o organizador renomeia o grupo para {string}")public void renomear(String nome){grupo.renomear(organizador,new NomeGrupo(nome));}
 @Então("o nome do grupo é {string}")public void nome(String nome){assertEquals(nome,grupo.getNome().toString());}
 @Quando("outro membro tenta renomear o grupo")public void outro(){try{grupo.renomear(membro,new NomeGrupo("Nome"));}catch(RuntimeException e){falha=e;}}
 @Então("a operação do grupo é negada")public void negada(){assertInstanceOf(NaoAutorizadoExcecao.class,falha);}
 @Quando("o organizador convida aluno de outra academia")public void outraAcademia(){try{grupo.convidar(organizador,new Aluno(Optional.of(new AlunoIdentificador("3")),"externo","Externo",new AcademiaIdentificador("2")),agora);}catch(RuntimeException e){falha=e;}}
 @Então("o convite entre academias é rejeitado")public void rejeitado(){assertInstanceOf(IllegalArgumentException.class,falha);assertTrue(grupo.getConvites().isEmpty());}
 @Quando("o organizador propõe um treino futuro com prazo válido")public void propor(){grupo.propor(organizador,agora.plusSeconds(18000),agora.plusSeconds(15000),agora);}
 @Então("cada membro recebe um convite de treino incluindo o organizador")public void todos(){var c=grupo.getTreinos().getLast().convites();assertEquals(2,c.size());assertEquals(Set.of(organizador,membro),new HashSet<>(c.stream().map(ConviteTreino::aluno).toList()));}
 @Quando("o organizador tenta responder o convite de treino de outro membro")public void falsificar(){try{grupo.responderTreino(1,2,organizador,true,agora);}catch(RuntimeException e){falha=e;}}
 @Quando("o membro confirma e o prazo de confirmação termina")public void expirar(){grupo.responderTreino(1,2,membro,true,agora);grupo.expirar(agora.plusSeconds(7200));grupo.expirar(agora.plusSeconds(7200));}
 @Então("a presença fica confirmada e a pendência expira")public void estados(){assertEquals(SituacaoConviteTreino.CONFIRMADO,grupo.treino(1).convite(2).situacao());assertEquals(SituacaoConviteTreino.EXPIRADO,grupo.treino(1).convite(1).situacao());}
 @Quando("o membro cancela sua presença exatamente uma hora antes")public void cancelar(){grupo.responderTreino(1,2,membro,true,agora);grupo.cancelarPresenca(1,2,membro,agora.plusSeconds(10800));}
 @Então("a presença cancelada preserva a data da confirmação")public void cancelada(){var c=grupo.treino(1).convite(2);assertEquals(SituacaoConviteTreino.CANCELADO,c.situacao());assertEquals(agora,c.resposta().orElseThrow());assertEquals(agora.plusSeconds(10800),c.cancelamento().orElseThrow());}
 @Quando("o organizador encerra o grupo")public void encerrar(){grupo.encerrar(organizador,agora);}
 @Então("os encontros futuros são cancelados e o histórico é preservado")public void historico(){assertTrue(grupo.getEncerramento().isPresent());assertTrue(grupo.treino(1).cancelado());assertEquals(2,grupo.getParticipacoes().size());assertTrue(grupo.getParticipacoes().stream().noneMatch(ParticipacaoGrupo::ativa));assertEquals(2,grupo.treino(1).convites().size());}
}
