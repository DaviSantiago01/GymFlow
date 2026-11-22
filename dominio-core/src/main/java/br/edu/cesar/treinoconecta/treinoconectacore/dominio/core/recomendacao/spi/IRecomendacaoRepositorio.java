package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.spi;

import java.time.LocalDate;
import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.Recomendacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.RecomendacaoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IRecomendacaoRepositorio extends IAgregadoRepositorio<RecomendacaoIdentificador, Recomendacao> {
    Recomendacao bloquear(RecomendacaoIdentificador identificador);

    /** Todas as recomendações do aluno, inclusive respondidas e expiradas. */
    List<Recomendacao> buscarPorAluno(AlunoIdentificador aluno);

    /** Recomendações em aberto cuja validade terminou antes do dia informado. */
    List<RecomendacaoIdentificador> buscarVencidas(LocalDate hoje, int limite);
}
