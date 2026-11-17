package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.MensalidadeIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

/** O filtro ATRASADA compara o vencimento com a data atual; o aluno autenticado só enxerga as próprias mensalidades. */
public interface IMensalidadeAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<MensalidadeProjecao, MensalidadeConsulta> {
    MensalidadeProjecao projetar(MensalidadeIdentificador identificador);
}
