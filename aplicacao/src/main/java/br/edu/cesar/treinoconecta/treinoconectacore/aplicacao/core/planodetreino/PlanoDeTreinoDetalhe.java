package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino;

import java.util.List;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;

public record PlanoDeTreinoDetalhe(PlanoDeTreinoProjecao plano, boolean gerenciavel,
        Optional<PlanoDeTreinoIdentificador> planoAnterior, List<TreinoDetalhe> treinos) { }
