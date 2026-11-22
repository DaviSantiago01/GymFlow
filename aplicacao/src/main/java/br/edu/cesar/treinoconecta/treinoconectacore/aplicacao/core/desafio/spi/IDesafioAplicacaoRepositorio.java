package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.desafio.spi;

import java.util.*;import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aulasdesafios.AulasDesafiosDados;import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.desafio.DesafioConsulta;import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IDesafioAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<AulasDesafiosDados.DesafioLinha,DesafioConsulta>{
 List<AulasDesafiosDados.PessoaLinha> alunos();List<AulasDesafiosDados.DesafioLinha> desafios();Optional<AulasDesafiosDados.DesafioLinha> desafio(long id);List<AulasDesafiosDados.AvaliacaoLinha> avaliacoes(long desafio);List<AulasDesafiosDados.EvolucaoLinha> evolucao(long desafio,String usuario);
}
