package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.grupotreino;

import java.time.Instant;
import java.util.*;
public final class DadosGrupos {
    private DadosGrupos(){}
    public record Pagina<T>(List<T> itens,long total,int pagina,int tamanho){public Pagina{itens=List.copyOf(itens);}}
    public record AlunoOpcao(String identificador,String nome,String usuario){}
    public record Contexto(String aluno,String nome,String academia,Instant agora){}
    public record Membro(String identificador,String aluno,String nome,Instant entrada,Optional<Instant> saida,boolean organizador){}
    public record ConviteEntrada(String identificador,String grupo,String grupoNome,String organizadorNome,String aluno,String alunoNome,String situacao,Instant envio,Optional<Instant> resposta){}
    public record Presenca(String identificador,String aluno,String nome,String situacao,Instant criacao,Optional<Instant> resposta,Optional<Instant> cancelamento,Optional<String> motivo){}
    public record TreinoResumo(String identificador,String grupo,String grupoNome,Instant inicio,Instant prazo,Optional<Instant> cancelamento,int convidados,int confirmados,int pendentes,Optional<Presenca> meuConvite,boolean souOrganizador,boolean membro,Instant agora,Optional<String> professor){}
    public record GrupoDetalhe(GrupoTreinoProjecao grupo,List<Membro> membros,List<ConviteEntrada> pendentes,Pagina<TreinoResumo> treinos,Instant agora){}
    public record ProfessorOpcao(String identificador,String nome){}
    /** Horário oferecido para treino de grupo: vagas restantes, se ainda aceita proposta e os professores livres nele. */
    public record HorarioOpcao(Instant inicio,Instant fim,int vagas,boolean aberto,List<ProfessorOpcao> professores){public HorarioOpcao{professores=List.copyOf(professores);}}
    public record TreinoDetalhe(TreinoResumo treino,List<Presenca> presencas){}
}
