package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino;

import java.time.*;
import java.util.*;
import java.util.function.BiConsumer;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.spi.IGrupoTreinoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.*;
import dev.sauloaraujo.d5.framework.dominio.spi.*;
public final class GrupoTreinoServico extends AgregadoServicoAbstrato<GrupoTreinoIdentificador,GrupoTreino,AgregadoAutorizacoes<GrupoTreino>> {
    private final IGrupoTreinoRepositorio grupos;private final IAlunoRepositorio alunos;private final Clock relogio;
    public GrupoTreinoServico(ISegurancaServico seguranca,IGrupoTreinoRepositorio grupos,IAlunoRepositorio alunos,Clock relogio){super(seguranca,grupos,"Grupo %s existente","Grupo %s inexistente");this.grupos=Objects.requireNonNull(grupos);this.alunos=Objects.requireNonNull(alunos);this.relogio=Objects.requireNonNull(relogio);}
    public Aluno autenticado(){if(!getSegurancaServico().possuiAutorizacao("USUARIO"))throw new NaoAutorizadoExcecao("Acesso reservado ao aluno");return alunos.buscarPorUsuario(getSegurancaServico().getUsuario());}
    public AgregadoAutorizacoes<GrupoTreino> criarAutorizacoes(Optional<GrupoTreinoIdentificador> id){boolean pode=getSegurancaServico().possuiAutorizacao("USUARIO");if(pode && id.isPresent())pode=grupos.buscarPorIdentificador(id.get()).podeConsultar(autenticado().getIdentificador().orElseThrow());return new AgregadoAutorizacoes<>(false,pode,false,false);}
    public GrupoTreino criar(NomeGrupo nome,List<AlunoIdentificador> convidados){var autor=autenticado();var agora=relogio.instant();var g=GrupoTreino.criar(nome,autor,agora);for(var id:List.copyOf(convidados))g.convidar(autor.getIdentificador().orElseThrow(),alunos.buscarPorIdentificador(id),agora);return grupos.incluir(g);}
    private void executar(GrupoTreinoIdentificador id,BiConsumer<GrupoTreino,Instant> acao){var g=grupos.bloquear(id);acao.accept(g,relogio.instant());grupos.alterar(g);}
    public void renomear(GrupoTreinoIdentificador id,NomeGrupo nome){var a=autenticado().getIdentificador().orElseThrow();executar(id,(g,t)->g.renomear(a,nome));}
    public void convidar(GrupoTreinoIdentificador id,AlunoIdentificador destinatario){var a=autenticado().getIdentificador().orElseThrow();executar(id,(g,t)->{g.exigirOrganizador(a);g.convidar(a,alunos.buscarPorIdentificador(destinatario),t);});}
    public void responderGrupo(GrupoTreinoIdentificador id,long convite,boolean aceitar){var a=autenticado();executar(id,(g,t)->g.responderConvite(convite,a,aceitar,t));}
    public void propor(GrupoTreinoIdentificador id,Instant inicio){var a=autenticado().getIdentificador().orElseThrow();executar(id,(g,t)->g.propor(a,inicio,t));}
    public void responderTreino(GrupoTreinoIdentificador id,long treino,long convite,boolean confirmar){var a=autenticado().getIdentificador().orElseThrow();executar(id,(g,t)->g.responderTreino(treino,convite,a,confirmar,t));}
    public void cancelarPresenca(GrupoTreinoIdentificador id,long treino,long convite){var a=autenticado().getIdentificador().orElseThrow();executar(id,(g,t)->g.cancelarPresenca(treino,convite,a,t));}
    public void cancelarTreino(GrupoTreinoIdentificador id,long treino){var a=autenticado().getIdentificador().orElseThrow();executar(id,(g,t)->g.cancelarTreino(treino,a,t));}
    public void sair(GrupoTreinoIdentificador id){var a=autenticado().getIdentificador().orElseThrow();executar(id,(g,t)->g.sair(a,t));}
    public void encerrar(GrupoTreinoIdentificador id){var a=autenticado().getIdentificador().orElseThrow();executar(id,(g,t)->g.encerrar(a,t));}
}
