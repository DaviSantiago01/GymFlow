package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.vinculopersonal.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.vinculopersonal.VinculoPersonalAlunoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.vinculopersonal.VinculoPersonalAlunoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAlunoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IVinculoPersonalAlunoAplicacaoRepositorio
        extends IAgregadoAplicacaoRepositorio<VinculoPersonalAlunoProjecao, VinculoPersonalAlunoConsulta> {
    VinculoPersonalAlunoProjecao projetar(VinculoPersonalAlunoIdentificador identificador);
}
