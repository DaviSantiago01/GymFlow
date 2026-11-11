package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.spi;

import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisica;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IAvaliacaoFisicaRepositorio extends IAgregadoRepositorio<AvaliacaoFisicaIdentificador, AvaliacaoFisica> {
    AvaliacaoFisica bloquear(AvaliacaoFisicaIdentificador identificador);
    List<AvaliacaoFisica> buscarValidasPorAluno(AlunoIdentificador aluno);
}
