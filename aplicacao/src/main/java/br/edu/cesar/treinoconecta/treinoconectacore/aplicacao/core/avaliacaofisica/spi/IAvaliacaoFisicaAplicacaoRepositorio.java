package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.spi;

import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.AvaliacaoFisicaConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.AvaliacaoFisicaProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IAvaliacaoFisicaAplicacaoRepositorio
        extends IAgregadoAplicacaoRepositorio<AvaliacaoFisicaProjecao, AvaliacaoFisicaConsulta> {
    AvaliacaoFisicaProjecao projetar(AvaliacaoFisicaIdentificador identificador);
    List<AvaliacaoFisicaProjecao> listarValidas(AlunoIdentificador aluno);
}
