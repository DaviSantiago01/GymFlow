package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor;

import java.time.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.spi.ITreinoRepositorio;
import java.util.*;
import io.cucumber.java.pt.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.spi.IProfessorRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor.spi.IAvaliacaoProfessorRepositorio;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class AvaliacaoProfessorStepDefinitions {
    final Instant criacao=Instant.parse("2026-11-05T12:00:00Z");
    final ProfessorIdentificador professor=new ProfessorIdentificador("1");
    AvaliacaoProfessor avaliacao;RuntimeException falha;
    IAvaliacaoProfessorRepositorio repositorio;AvaliacaoProfessorServico servico;
    @Quando("registrar as notas 0, 1, 2, 3 e 5 sem comentário") public void criar() { avaliacao=AvaliacaoProfessor.criar(new AlunoIdentificador("1"),professor,new TreinoIdentificador("1"),new NotasAvaliacao(0,1,2,3,5),Optional.empty(),criacao); }
    @Então("a avaliação de domínio contém as cinco notas informadas") public void notas(){ assertEquals(new NotasAvaliacao(0,1,2,3,5),avaliacao.notas()); }
    @Então("a avaliação de domínio não contém comentário") public void comentario(){ assertTrue(avaliacao.comentario().isEmpty()); }
    @Quando("registrar auxílio com nota {int}") public void notaInvalida(int n){ try{new NotasAvaliacao(n,3,3,3,3);}catch(RuntimeException e){falha=e;} }
    @Então("a avaliação de domínio é rejeitada") public void rejeitada(){assertNotNull(falha);}
    @Quando("registrar um comentário com 501 caracteres") public void longo(){try{avaliacao=AvaliacaoProfessor.criar(new AlunoIdentificador("1"),professor,new TreinoIdentificador("1"),new NotasAvaliacao(3,3,3,3,3),Optional.of("a".repeat(501)),criacao);}catch(RuntimeException e){falha=e;}}
    @Dado("que o aluno já avaliou o treino") public void duplicada(){
        var seguranca=mock(ISegurancaServico.class);var alunos=mock(IAlunoRepositorio.class);var treinos=mock(ITreinoRepositorio.class);repositorio=mock(IAvaliacaoProfessorRepositorio.class);
        when(seguranca.possuiAutorizacao("USUARIO")).thenReturn(true);when(seguranca.getUsuario()).thenReturn("u");
        when(alunos.bloquearPorUsuario("u")).thenReturn(new Aluno(Optional.of(new AlunoIdentificador("1")),"u","Aluno"));
        when(repositorio.jaAvaliou(new TreinoIdentificador("1"))).thenReturn(true);
        when(treinos.bloquear(new TreinoIdentificador("1"))).thenReturn(new Treino(Optional.of(new TreinoIdentificador("1")),new AlunoIdentificador("1"),professor,"Treino",criacao.minusSeconds(7200),criacao.minusSeconds(3600),true,false));
        servico=new AvaliacaoProfessorServico(seguranca,repositorio,alunos,treinos,Clock.fixed(criacao,ZoneOffset.UTC));
    }
    @Quando("o aluno tentar avaliar novamente esse treino") public void avaliar(){try{servico.avaliar(new TreinoIdentificador("1"),new NotasAvaliacao(3,3,3,3,3),Optional.empty());}catch(RuntimeException e){falha=e;}}
    @Então("a avaliação duplicada não é persistida") public void naoPersistida(){assertInstanceOf(IllegalStateException.class,falha);verify(repositorio,never()).incluir(any());verify(repositorio,never()).registrarParticipacao(any());}
    @Dado("uma avaliação de domínio do aluno {string}") public void doAutor(String autor){avaliacao=AvaliacaoProfessor.criar(new AlunoIdentificador(autor),professor,new TreinoIdentificador("1"),new NotasAvaliacao(3,3,3,3,3),Optional.empty(),criacao);}
    @Quando("o aluno {string} tentar editar a avaliação de domínio") public void editar(String autor){try{avaliacao=avaliacao.editar(new AlunoIdentificador(autor),new NotasAvaliacao(5,5,5,5,5),Optional.empty(),criacao.plusSeconds(60));}catch(RuntimeException e){falha=e;}}
    @Quando("o aluno {string} tentar excluir a avaliação de domínio") public void excluir(String autor){try{avaliacao.exigirAutor(new AlunoIdentificador(autor));}catch(RuntimeException e){falha=e;}}
    @Então("a data de criação é preservada e a atualização é registrada") public void datas(){assertEquals(criacao,avaliacao.criacao());assertEquals(criacao.plusSeconds(60),avaliacao.atualizacao());}
}
