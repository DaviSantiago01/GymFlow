package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aulasdesafios;

import java.math.BigDecimal;import java.time.*;import org.jspecify.annotations.Nullable;import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aula.Aula;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public final class AulasDesafiosDados {
 private AulasDesafiosDados(){}
 public record AulaLinha(long identificador,String titulo,String modalidade,@Nullable String descricao,LocalDate data,LocalTime inicio,LocalTime fim,int limite,int ocupacao,String status,long professor,String professorNome,boolean inscrito) implements IProjecao<Aula>{}
 public record DesafioLinha(long identificador,String titulo,String indicador,LocalDate inicio,LocalDate fim,String status,int participantes) implements dev.sauloaraujo.d5.framework.aplicacao.IProjecao<br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.desafio.Desafio>{}
 public record PessoaLinha(long identificador,String nome){}
 public record InscricaoLinha(long identificador,String titulo,String modalidade,LocalDate data,LocalTime inicio,String professor,String status,OffsetDateTime registro,String nome){ }
 public record NotificacaoLinha(long identificador,String aula,String aluno,String mensagem,OffsetDateTime registro,boolean lida){}
 public record AvaliacaoLinha(long identificador,long desafio,long aluno,String alunoNome,long profissional,String profissionalNome,String tipo,BigDecimal peso,BigDecimal cintura,BigDecimal gordura,OffsetDateTime registro,boolean bloqueada){}
 public record EvolucaoLinha(long aluno,String nome,String indicador,BigDecimal meta,@Nullable BigDecimal pesoInicial,@Nullable BigDecimal pesoAtual,@Nullable BigDecimal cinturaInicial,@Nullable BigDecimal cinturaAtual,@Nullable BigDecimal gorduraInicial,@Nullable BigDecimal gorduraAtual){}
}
