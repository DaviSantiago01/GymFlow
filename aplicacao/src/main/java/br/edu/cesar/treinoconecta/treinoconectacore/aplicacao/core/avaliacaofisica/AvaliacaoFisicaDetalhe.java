package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica;

import java.util.List;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.MedidaCorporal;

public record AvaliacaoFisicaDetalhe(AvaliacaoFisicaProjecao avaliacao, List<MedidaCorporal> medidas,
        Optional<AvaliacaoFisicaIdentificador> referencia, Optional<AvaliacaoFisicaIdentificador> versaoAnterior,
        boolean gerenciavel) { }
