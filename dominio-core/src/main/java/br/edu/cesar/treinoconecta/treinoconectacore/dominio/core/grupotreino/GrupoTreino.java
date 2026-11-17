package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino;

import java.time.Instant;
import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.academia.AcademiaIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;
public final class GrupoTreino implements IAgregado<GrupoTreinoIdentificador> {
    private final Optional<GrupoTreinoIdentificador> identificador;
    private NomeGrupo nome;
    private final AlunoIdentificador organizador;
    private final AcademiaIdentificador academia;
    private final Instant criacao;
    private Optional<Instant> encerramento;
    private final List<ParticipacaoGrupo> participacoes;
    private final List<ConviteGrupo> convites;
    private final List<TreinoGrupo> treinos;
    public GrupoTreino(Optional<GrupoTreinoIdentificador> identificador,NomeGrupo nome,AlunoIdentificador organizador,AcademiaIdentificador academia,Instant criacao,Optional<Instant> encerramento,List<ParticipacaoGrupo> participacoes,List<ConviteGrupo> convites,List<TreinoGrupo> treinos){
        this.identificador=Objects.requireNonNull(identificador);this.nome=Objects.requireNonNull(nome);this.organizador=Objects.requireNonNull(organizador);this.academia=Objects.requireNonNull(academia);this.criacao=Objects.requireNonNull(criacao);this.encerramento=Objects.requireNonNull(encerramento);this.participacoes=new ArrayList<>(List.copyOf(participacoes));this.convites=new ArrayList<>(List.copyOf(convites));this.treinos=new ArrayList<>(List.copyOf(treinos));
    }
    public static GrupoTreino criar(NomeGrupo nome,Aluno autor,Instant agora){var id=autor.getIdentificador().orElseThrow();return new GrupoTreino(Optional.empty(),nome,id,autor.getAcademia(),agora,Optional.empty(),List.of(new ParticipacaoGrupo(Optional.empty(),id,agora,Optional.empty())),List.of(),List.of());}
    public Optional<GrupoTreinoIdentificador> getIdentificador(){return identificador;}
    public NomeGrupo getNome(){return nome;} public AlunoIdentificador getOrganizador(){return organizador;} public AcademiaIdentificador getAcademia(){return academia;}
    public Instant getCriacao(){return criacao;} public Optional<Instant> getEncerramento(){return encerramento;}
    public List<ParticipacaoGrupo> getParticipacoes(){return List.copyOf(participacoes);} public List<ConviteGrupo> getConvites(){return List.copyOf(convites);} public List<TreinoGrupo> getTreinos(){return List.copyOf(treinos);}
    public boolean membro(AlunoIdentificador aluno){return participacoes.stream().anyMatch(p->p.aluno().equals(aluno) && p.ativa());}
    public boolean podeConsultar(AlunoIdentificador aluno){return membro(aluno) || encerramento.isPresent() && participacoes.stream().anyMatch(p->p.aluno().equals(aluno));}
    public void exigirAtivo(){if(encerramento.isPresent())throw new IllegalStateException("Grupo encerrado");}
    public void exigirOrganizador(AlunoIdentificador autor){if(!organizador.equals(autor))throw new NaoAutorizadoExcecao("Somente o organizador pode administrar o grupo");exigirAtivo();}
    public void exigirMembro(AlunoIdentificador autor){if(!membro(autor))throw new NaoAutorizadoExcecao("Você não participa deste grupo");exigirAtivo();}
    public void renomear(AlunoIdentificador autor,NomeGrupo novo){exigirOrganizador(autor);nome=Objects.requireNonNull(novo);}
    public void convidar(AlunoIdentificador autor,Aluno destinatario,Instant agora){
        exigirOrganizador(autor);var aluno=destinatario.getIdentificador().orElseThrow();
        if(!academia.equals(destinatario.getAcademia()))throw new IllegalArgumentException("Convide somente alunos da mesma academia");
        if(membro(aluno) || convites.stream().anyMatch(c->c.aluno().equals(aluno) && c.situacao()==SituacaoConviteGrupo.PENDENTE))throw new IllegalStateException("Aluno já participa do grupo ou possui convite pendente");
        convites.add(new ConviteGrupo(Optional.empty(),aluno,SituacaoConviteGrupo.PENDENTE,agora,Optional.empty()));
    }
    public void responderConvite(long id,Aluno autor,boolean aceitar,Instant agora){
        exigirAtivo();var aluno=autor.getIdentificador().orElseThrow();
        var c=convites.stream().filter(v->v.identificador().filter(i->i==id).isPresent()).findFirst().orElseThrow(()->new NoSuchElementException("Convite de grupo inexistente"));
        var resposta=c.responder(aluno,aceitar,agora);
        if(!academia.equals(autor.getAcademia()))throw new NaoAutorizadoExcecao("Aluno de outra academia");
        if(aceitar){if(membro(aluno))throw new IllegalStateException("Aluno já participa do grupo");participacoes.add(new ParticipacaoGrupo(Optional.empty(),aluno,agora,Optional.empty()));}
        convites.set(convites.indexOf(c),resposta);
    }
    public void propor(AlunoIdentificador autor,Instant inicio,Instant agora){exigirOrganizador(autor);treinos.add(TreinoGrupo.propor(inicio,agora,participacoes.stream().filter(ParticipacaoGrupo::ativa).map(ParticipacaoGrupo::aluno).toList()));}
    public TreinoGrupo treino(long id){return treinos.stream().filter(t->t.identificador().filter(i->i==id).isPresent()).findFirst().orElseThrow(()->new NoSuchElementException("Treino do grupo inexistente"));}
    public void responderTreino(long id,long convite,AlunoIdentificador autor,boolean confirmar,Instant agora){exigirMembro(autor);var t=treino(id);treinos.set(treinos.indexOf(t),t.responder(convite,autor,confirmar,agora));}
    public void cancelarPresenca(long id,long convite,AlunoIdentificador autor,Instant agora){exigirMembro(autor);var t=treino(id);treinos.set(treinos.indexOf(t),t.cancelarPresenca(convite,autor,agora));}
    public void cancelarTreino(long id,AlunoIdentificador autor,Instant agora){exigirOrganizador(autor);var t=treino(id);treinos.set(treinos.indexOf(t),t.cancelar(agora));}
    public void sair(AlunoIdentificador autor,Instant agora){
        exigirMembro(autor);if(organizador.equals(autor))throw new IllegalStateException("O organizador deve encerrar o grupo para deixar a administração");
        if(treinos.stream().anyMatch(t->!t.cancelado() && t.inicio().isAfter(agora) && t.convites().stream().anyMatch(c->c.aluno().equals(autor) && c.situacao()==SituacaoConviteTreino.CONFIRMADO)))throw new IllegalStateException("Cancele sua presença nos treinos futuros antes de sair");
        participacoes.replaceAll(p->p.aluno().equals(autor) && p.ativa()?p.sair(agora):p);
        convites.replaceAll(c->c.aluno().equals(autor)?c.cancelar(agora):c);
        treinos.replaceAll(t->t.retirar(autor,agora));
    }
    public void encerrar(AlunoIdentificador autor,Instant agora){
        exigirOrganizador(autor);treinos.replaceAll(t->!t.cancelado() && t.inicio().isAfter(agora)?t.cancelar(agora):t.expirar(agora));
        convites.replaceAll(c->c.cancelar(agora));participacoes.replaceAll(p->p.ativa()?p.sair(agora):p);encerramento=Optional.of(agora);
    }
    public void expirar(Instant agora){treinos.replaceAll(t->t.expirar(agora));}
}
