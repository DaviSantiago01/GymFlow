package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.grupotreino;

import java.time.*;
import java.util.*;
import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.spi.IGrupoTreinoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.academia.AcademiaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.academia.AcademiaJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;
public final class GrupoTreinoRepositorio extends AgregadoRepositorioAbstrato<GrupoTreinoIdentificador,GrupoTreino,Long,GrupoTreinoJpa> implements IGrupoTreinoRepositorio {
    private final IGrupoTreinoRepositorioJpa jpa;private final EntityManager em;private final JdbcTemplate banco;
    public GrupoTreinoRepositorio(IGrupoTreinoRepositorioJpa jpa,EntityManager em,JdbcTemplate banco){super(jpa,"Grupo %s inexistente");this.jpa=Objects.requireNonNull(jpa);this.em=Objects.requireNonNull(em);this.banco=Objects.requireNonNull(banco);}
    protected Long paraIdentificadorJpa(GrupoTreinoIdentificador id){return Long.valueOf(id.toString());}
    private AlunoJpa aluno(AlunoIdentificador id){return em.getReference(AlunoJpa.class,Long.valueOf(id.toString()));}
    private AlunoIdentificador aluno(AlunoJpa a){return new AlunoIdentificador(a.getIdentificador().toString());}
    protected GrupoTreinoJpa paraJpa(GrupoTreino g){
        var j=new GrupoTreinoJpa();j.identificador=g.getIdentificador().map(this::paraIdentificadorJpa).orElse(null);j.nome=g.getNome().toString();j.organizador=aluno(g.getOrganizador());j.academia=em.getReference(AcademiaJpa.class,Long.valueOf(g.getAcademia().toString()));j.criacao=g.getCriacao();j.encerramento=g.getEncerramento().orElse(null);
        for(var p:g.getParticipacoes()){var v=new ParticipacaoGrupoJpa();v.identificador=p.identificador().orElse(null);v.grupo=j;v.aluno=aluno(p.aluno());v.academia=j.academia;v.entrada=p.entrada();v.saida=p.saida().orElse(null);j.participacoes.add(v);}
        for(var c:g.getConvites()){var v=new ConviteGrupoJpa();v.identificador=c.identificador().orElse(null);v.grupo=j;v.aluno=aluno(c.aluno());v.academia=j.academia;v.situacao=c.situacao();v.envio=c.envio();v.resposta=c.resposta().orElse(null);j.convites.add(v);}
        for(var t:g.getTreinos()){var v=new TreinoGrupoJpa();v.identificador=t.identificador().orElse(null);v.grupo=j;v.inicio=t.inicio();v.prazo=t.prazo();v.criacao=t.criacao();v.cancelamento=t.cancelamento().orElse(null);
            for(var c:t.convites()){var k=new ConviteTreinoJpa();k.identificador=c.identificador().orElse(null);k.treino=v;k.aluno=aluno(c.aluno());k.situacao=c.situacao();k.criacao=c.criacao();k.resposta=c.resposta().orElse(null);k.cancelamento=c.cancelamento().orElse(null);k.motivo=c.motivo().orElse(null);v.convites.add(k);}j.treinos.add(v);
        }return j;
    }
    protected GrupoTreino paraAgregado(GrupoTreinoJpa g){return new GrupoTreino(Optional.of(new GrupoTreinoIdentificador(Objects.requireNonNull(g.identificador).toString())),new NomeGrupo(g.nome),aluno(g.organizador),new AcademiaIdentificador(g.academia.getIdentificador().toString()),g.criacao,Optional.ofNullable(g.encerramento),
        g.participacoes.stream().map(p->new ParticipacaoGrupo(Optional.ofNullable(p.identificador),aluno(p.aluno),p.entrada,Optional.ofNullable(p.saida))).toList(),
        g.convites.stream().map(c->new ConviteGrupo(Optional.ofNullable(c.identificador),aluno(c.aluno),c.situacao,c.envio,Optional.ofNullable(c.resposta))).toList(),
        g.treinos.stream().map(t->new TreinoGrupo(Optional.ofNullable(t.identificador),t.inicio,t.prazo,t.criacao,Optional.ofNullable(t.cancelamento),t.convites.stream().map(c->new ConviteTreino(Optional.ofNullable(c.identificador),aluno(c.aluno),c.situacao,c.criacao,Optional.ofNullable(c.resposta),Optional.ofNullable(c.cancelamento),Optional.ofNullable(c.motivo))).toList())).toList());}
    public GrupoTreino bloquear(GrupoTreinoIdentificador id){return paraAgregado(jpa.bloquear(paraIdentificadorJpa(id)).orElseThrow(()->new NoSuchElementException("Grupo inexistente")));}
    public List<GrupoTreinoIdentificador> vencidos(Instant agora,int limite){return banco.query("select distinct t.GRT_ID from TGR_TREINO_GRUPO t join CTR_CONVITE_TREINO c on c.TGR_ID=t.TGR_ID where t.TGR_PRAZO<=? and c.CTR_SITUACAO='PENDENTE' order by t.GRT_ID limit ?",(r,n)->new GrupoTreinoIdentificador(r.getString(1)),agora,limite);}
}
