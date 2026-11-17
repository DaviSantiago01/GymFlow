package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade;

import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.Pagamento;

/** A mensalidade com o histórico completo de pagamentos, inclusive os estornados. */
public record MensalidadeDetalhe(MensalidadeProjecao mensalidade, List<Pagamento> pagamentos) { }
