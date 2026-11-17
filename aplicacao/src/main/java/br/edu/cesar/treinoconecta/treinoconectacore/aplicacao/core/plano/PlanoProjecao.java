package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano;

import java.math.BigDecimal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.Plano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.SituacaoPlano;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public record PlanoProjecao(PlanoIdentificador identificador, String nome, BigDecimal valor, int duracaoMeses,
        SituacaoPlano situacao, long adesoesAtivas) implements IProjecao<Plano> {
    public PlanoProjecao(Long identificador, String nome, BigDecimal valor, Integer duracaoMeses, SituacaoPlano situacao,
            Long adesoesAtivas) {
        this(new PlanoIdentificador(identificador.toString()), nome, valor, duracaoMeses, situacao, adesoesAtivas);
    }
}
