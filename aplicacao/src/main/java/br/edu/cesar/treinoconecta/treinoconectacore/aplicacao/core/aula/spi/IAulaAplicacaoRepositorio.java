package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aula.spi;

import java.time.LocalDate;import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aulasdesafios.*;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IAulaAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<AulasDesafiosDados.AulaLinha,AulasDesafiosConsulta>{
 List<AulasDesafiosDados.AulaLinha> aulas(Optional<LocalDate> data,Optional<String> modalidade,Optional<String> busca,String usuario);
 Optional<AulasDesafiosDados.AulaLinha> aula(long id,String usuario);
 List<AulasDesafiosDados.InscricaoLinha> minhasAulas(String usuario);
 List<AulasDesafiosDados.InscricaoLinha> inscritos(long aula);
 List<AulasDesafiosDados.NotificacaoLinha> notificacoes(String usuario);
 List<AulasDesafiosDados.PessoaLinha> professores();
}
