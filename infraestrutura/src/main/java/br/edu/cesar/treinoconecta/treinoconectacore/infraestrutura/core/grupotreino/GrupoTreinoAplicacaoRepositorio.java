package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.grupotreino;

import java.time.*;
import java.sql.*;
import java.util.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.data.domain.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.grupotreino.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.grupotreino.DadosGrupos.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.grupotreino.spi.IGrupoTreinoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.GrupoTreinoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.*;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;
public final class GrupoTreinoAplicacaoRepositorio extends AgregadoAplicacaoRepositorioAbstrato<GrupoTreinoProjecao,GrupoTreinoOrdenacao,GrupoTreinoConsulta> implements IGrupoTreinoAplicacaoRepositorio {
    private final NamedParameterJdbcTemplate banco;private final ISegurancaServico seguranca;private final Clock relogio;
    public GrupoTreinoAplicacaoRepositorio(JdbcTemplate banco,ISegurancaServico seguranca,Clock relogio){this.banco=new NamedParameterJdbcTemplate(Objects.requireNonNull(banco));this.seguranca=Objects.requireNonNull(seguranca);this.relogio=Objects.requireNonNull(relogio);}
    private MapSqlParameterSource parametros(AlunoIdentificador aluno){return new MapSqlParameterSource("aluno",Long.valueOf(aluno.toString())).addValue("agora",relogio.instant());}
    private static Instant instante(ResultSet r,String coluna)throws SQLException{return r.getObject(coluna,OffsetDateTime.class).toInstant();}
    private static Optional<Instant> opcional(ResultSet r,String coluna)throws SQLException{return Optional.ofNullable(r.getObject(coluna,OffsetDateTime.class)).map(OffsetDateTime::toInstant);}
    private static final String MEMBRO="exists(select 1 from PGR_PARTICIPACAO_GRUPO p where p.GRT_ID=g.GRT_ID and p.ALU_ID=:aluno and p.PGR_SAIDA is null)";
    private static final String ACESSO="("+MEMBRO+" or (g.GRT_ENCERRAMENTO is not null and exists(select 1 from PGR_PARTICIPACAO_GRUPO p where p.GRT_ID=g.GRT_ID and p.ALU_ID=:aluno)))";
    private static final String GRUPOS="""
        select g.*,a.ALU_NOME organizador_nome,d.ACD_NOME academia_nome,
        (select count(*) from PGR_PARTICIPACAO_GRUPO p where p.GRT_ID=g.GRT_ID and p.PGR_SAIDA is null) membros,
        (select min(t.TGR_INICIO) from TGR_TREINO_GRUPO t where t.GRT_ID=g.GRT_ID and t.TGR_CANCELAMENTO is null and t.TGR_INICIO>:agora) proximo
        from GRT_GRUPO_TREINO g join ALU_ALUNO a on a.ALU_ID=g.ALU_ORGANIZADOR join ACD_ACADEMIA d on d.ACD_ID=g.ACD_ID
        """;
    private RowMapper<GrupoTreinoProjecao> grupo(AlunoIdentificador aluno){return (r,n)->new GrupoTreinoProjecao(r.getString("GRT_ID"),r.getString("GRT_NOME"),r.getString("ALU_ORGANIZADOR"),r.getString("organizador_nome"),r.getString("academia_nome"),r.getString("ALU_ORGANIZADOR").equals(aluno.toString()),r.getInt("membros"),opcional(r,"proximo"),opcional(r,"GRT_ENCERRAMENTO"));}
    protected Page<GrupoTreinoProjecao> pesquisar(GrupoTreinoConsulta consulta,Pageable pagina){
        var aluno=new AlunoIdentificador(Objects.requireNonNull(banco.queryForObject("select ALU_ID from ALU_ALUNO where ALU_USUARIO=:usuario",Map.of("usuario",seguranca.getUsuario()),String.class)));
        var p=parametros(aluno).addValue("limite",pagina.getPageSize()).addValue("offset",pagina.getOffset());
        var itens=banco.query(GRUPOS+" where "+ACESSO+" order by g.GRT_ENCERRAMENTO nulls first,lower(g.GRT_NOME),g.GRT_ID limit :limite offset :offset",p,grupo(aluno));
        var total=banco.queryForObject("select count(*) from GRT_GRUPO_TREINO g where "+ACESSO,p,Long.class);return new PageImpl<>(itens,pagina,Objects.requireNonNull(total));
    }
    protected String paraPropriedadeOrdenacao(GrupoTreinoOrdenacao o){return "nome";}
    public Contexto contexto(AlunoIdentificador aluno){return banco.queryForObject("select a.ALU_NOME,d.ACD_NOME from ALU_ALUNO a join ACD_ACADEMIA d on d.ACD_ID=a.ACD_ID where ALU_ID=:aluno",parametros(aluno),(r,n)->new Contexto(aluno.toString(),r.getString(1),r.getString(2),relogio.instant()));}
    private void pagina(int pagina){org.apache.commons.lang3.Validate.isTrue(pagina>=0 && pagina<=100000,"Página inválida");}
    public Pagina<AlunoOpcao> alunos(AlunoIdentificador aluno,String busca,int pagina,Optional<GrupoTreinoIdentificador> grupo){
        pagina(pagina);org.apache.commons.lang3.Validate.isTrue(busca.length()<=100,"Busca muito longa");
        var p=parametros(aluno).addValue("busca",busca.strip().toLowerCase(Locale.ROOT)).addValue("offset",pagina*20);
        String filtro="a.ACD_ID=(select ACD_ID from ALU_ALUNO where ALU_ID=:aluno) and a.ALU_ID<>:aluno and lower(a.ALU_USUARIO)=:busca";
        if(grupo.isPresent()){var g=exigirAcesso(grupo.get(),aluno);if(!g.souOrganizador() || g.encerramento().isPresent())throw new NaoAutorizadoExcecao("Somente o organizador pode selecionar convidados");p.addValue("grupo",Long.valueOf(grupo.get().toString()));filtro+=" and not exists(select 1 from PGR_PARTICIPACAO_GRUPO p where p.GRT_ID=:grupo and p.ALU_ID=a.ALU_ID and p.PGR_SAIDA is null) and not exists(select 1 from CGR_CONVITE_GRUPO c where c.GRT_ID=:grupo and c.ALU_ID=a.ALU_ID and c.CGR_SITUACAO='PENDENTE')";}
        return new Pagina<>(banco.query("select a.ALU_ID,a.ALU_NOME,a.ALU_USUARIO from ALU_ALUNO a where "+filtro+" order by lower(a.ALU_NOME),a.ALU_ID limit 20 offset :offset",p,(r,n)->new AlunoOpcao(r.getString(1),r.getString(2),r.getString(3))),Objects.requireNonNull(banco.queryForObject("select count(*) from ALU_ALUNO a where "+filtro,p,Long.class)),pagina,20);
    }
    private GrupoTreinoProjecao exigirAcesso(GrupoTreinoIdentificador id,AlunoIdentificador aluno){var itens=banco.query(GRUPOS+" where g.GRT_ID=:grupo and "+ACESSO,parametros(aluno).addValue("grupo",Long.valueOf(id.toString())),grupo(aluno));if(itens.isEmpty())throw new NaoAutorizadoExcecao("Grupo indisponível: somente membros podem consultar seus detalhes");return itens.getFirst();}
    private static final String ENTRADAS="select c.*,g.GRT_NOME,o.ALU_NOME organizador_nome,a.ALU_NOME aluno_nome from CGR_CONVITE_GRUPO c join GRT_GRUPO_TREINO g on g.GRT_ID=c.GRT_ID join ALU_ALUNO o on o.ALU_ID=g.ALU_ORGANIZADOR join ALU_ALUNO a on a.ALU_ID=c.ALU_ID";
    private static final RowMapper<ConviteEntrada> ENTRADA=(r,n)->new ConviteEntrada(r.getString("CGR_ID"),r.getString("GRT_ID"),r.getString("GRT_NOME"),r.getString("organizador_nome"),r.getString("ALU_ID"),r.getString("aluno_nome"),r.getString("CGR_SITUACAO"),instante(r,"CGR_ENVIO"),opcional(r,"CGR_RESPOSTA"));
    public GrupoDetalhe detalhe(GrupoTreinoIdentificador id,AlunoIdentificador aluno,boolean historico,int pagina){
        pagina(pagina);var g=exigirAcesso(id,aluno);var p=parametros(aluno).addValue("grupo",Long.valueOf(id.toString()));
        var membros=banco.query("select p.*,a.ALU_NOME from PGR_PARTICIPACAO_GRUPO p join ALU_ALUNO a on a.ALU_ID=p.ALU_ID where p.GRT_ID=:grupo order by p.PGR_SAIDA nulls first,p.PGR_ID",p,(r,n)->new Membro(r.getString("PGR_ID"),r.getString("ALU_ID"),r.getString("ALU_NOME"),instante(r,"PGR_ENTRADA"),opcional(r,"PGR_SAIDA"),r.getString("ALU_ID").equals(g.organizador())));
        var pendentes=banco.query(ENTRADAS+" where c.GRT_ID=:grupo and c.CGR_SITUACAO='PENDENTE' order by c.CGR_ID",p,ENTRADA);
        var treinos=treinos(p,"t.GRT_ID=:grupo and "+(historico?"(t.TGR_INICIO<=:agora or t.TGR_CANCELAMENTO is not null)":"t.TGR_INICIO>:agora and t.TGR_CANCELAMENTO is null"),pagina,historico);
        return new GrupoDetalhe(g,membros,pendentes,treinos,relogio.instant());
    }
    public Pagina<ConviteEntrada> convites(AlunoIdentificador aluno,int pagina,boolean historico){pagina(pagina);var p=parametros(aluno).addValue("offset",pagina*20);String filtro="c.ALU_ID=:aluno and c.CGR_SITUACAO"+(historico?"<>'PENDENTE'":"='PENDENTE'");return new Pagina<>(banco.query(ENTRADAS+" where "+filtro+" order by c.CGR_ENVIO desc,c.CGR_ID desc limit 20 offset :offset",p,ENTRADA),Objects.requireNonNull(banco.queryForObject("select count(*) from CGR_CONVITE_GRUPO c where "+filtro,p,Long.class)),pagina,20);}
    private static final String TREINOS="""
        select t.*,g.GRT_NOME,g.ALU_ORGANIZADOR,
        (select count(*) from CTR_CONVITE_TREINO c where c.TGR_ID=t.TGR_ID) convidados,
        (select count(*) from CTR_CONVITE_TREINO c where c.TGR_ID=t.TGR_ID and c.CTR_SITUACAO='CONFIRMADO') confirmados,
        (select count(*) from CTR_CONVITE_TREINO c where c.TGR_ID=t.TGR_ID and c.CTR_SITUACAO='PENDENTE' and t.TGR_PRAZO>:agora) pendentes,
        (select count(*) from PGR_PARTICIPACAO_GRUPO p where p.GRT_ID=t.GRT_ID and p.ALU_ID=:aluno and p.PGR_SAIDA is null) membro,
        m.CTR_ID,m.ALU_ID,m.CTR_SITUACAO,m.CTR_CRIACAO,m.CTR_RESPOSTA,m.CTR_CANCELAMENTO,m.CTR_MOTIVO,a.ALU_NOME,pr.PRF_NOME
        from TGR_TREINO_GRUPO t join GRT_GRUPO_TREINO g on g.GRT_ID=t.GRT_ID left join PRF_PROFISSIONAL pr on pr.PRF_ID=t.PRF_ID
        left join CTR_CONVITE_TREINO m on m.TGR_ID=t.TGR_ID and m.ALU_ID=:aluno left join ALU_ALUNO a on a.ALU_ID=m.ALU_ID
        """;
    private Presenca presenca(ResultSet r,Instant agora)throws SQLException{String situacao=r.getString("CTR_SITUACAO");if(situacao.equals("PENDENTE") && !agora.isBefore(instante(r,"TGR_PRAZO")))situacao="EXPIRADO";return new Presenca(r.getString("CTR_ID"),r.getString("ALU_ID"),r.getString("ALU_NOME"),situacao,instante(r,"CTR_CRIACAO"),opcional(r,"CTR_RESPOSTA"),opcional(r,"CTR_CANCELAMENTO"),Optional.ofNullable(r.getString("CTR_MOTIVO")));}
    private RowMapper<TreinoResumo> treino(MapSqlParameterSource p){Instant agora=(Instant)Objects.requireNonNull(p.getValue("agora"));String aluno=Objects.requireNonNull(p.getValue("aluno")).toString();return(r,n)->new TreinoResumo(r.getString("TGR_ID"),r.getString("GRT_ID"),r.getString("GRT_NOME"),instante(r,"TGR_INICIO"),instante(r,"TGR_PRAZO"),opcional(r,"TGR_CANCELAMENTO"),r.getInt("convidados"),r.getInt("confirmados"),r.getInt("pendentes"),r.getObject("CTR_ID")==null?Optional.empty():Optional.of(presenca(r,agora)),r.getString("ALU_ORGANIZADOR").equals(aluno),r.getInt("membro")>0,agora,Optional.ofNullable(r.getString("PRF_NOME")));}
    private Pagina<TreinoResumo> treinos(MapSqlParameterSource p,String filtro,int pagina,boolean historico){p.addValue("offset",pagina*20);return new Pagina<>(banco.query(TREINOS+" where "+filtro+" order by t.TGR_INICIO "+(historico?"desc":"asc")+",t.TGR_ID limit 20 offset :offset",p,treino(p)),Objects.requireNonNull(banco.queryForObject("select count(*) from TGR_TREINO_GRUPO t where "+filtro,p,Long.class)),pagina,20);}
    public Pagina<TreinoResumo> convitesTreino(AlunoIdentificador aluno,int pagina,boolean historico){pagina(pagina);return treinos(parametros(aluno),"exists(select 1 from CTR_CONVITE_TREINO c where c.TGR_ID=t.TGR_ID and c.ALU_ID=:aluno) and "+(historico?"(t.TGR_INICIO<=:agora or t.TGR_CANCELAMENTO is not null)":"t.TGR_INICIO>:agora and t.TGR_CANCELAMENTO is null"),pagina,historico);}
    /** Confirmou presença: convite confirmado, ou cancelado depois de respondido (desistência ou treino cancelado). */
    public List<TreinoResumo> agendamentos(AlunoIdentificador aluno){var p=parametros(aluno);return banco.query(TREINOS+" where m.CTR_RESPOSTA is not null and m.CTR_SITUACAO in ('CONFIRMADO','CANCELADO') order by case when t.TGR_INICIO>:agora and t.TGR_CANCELAMENTO is null then 0 else 1 end,case when t.TGR_INICIO>:agora and t.TGR_CANCELAMENTO is null then t.TGR_INICIO end asc,t.TGR_INICIO desc,t.TGR_ID limit 100",p,treino(p));}
    public TreinoDetalhe treino(GrupoTreinoIdentificador id,long treino,AlunoIdentificador aluno){exigirAcesso(id,aluno);var p=parametros(aluno).addValue("grupo",Long.valueOf(id.toString())).addValue("treino",treino);var itens=banco.query(TREINOS+" where t.TGR_ID=:treino and t.GRT_ID=:grupo",p,treino(p));if(itens.isEmpty())throw new NoSuchElementException("Treino inexistente neste grupo");return new TreinoDetalhe(itens.getFirst(),banco.query("select c.*,a.ALU_NOME,t.TGR_PRAZO from CTR_CONVITE_TREINO c join ALU_ALUNO a on a.ALU_ID=c.ALU_ID join TGR_TREINO_GRUPO t on t.TGR_ID=c.TGR_ID where c.TGR_ID=:treino order by a.ALU_NOME,c.CTR_ID",p,(r,n)->presenca(r,(Instant)Objects.requireNonNull(p.getValue("agora")))));}
}
