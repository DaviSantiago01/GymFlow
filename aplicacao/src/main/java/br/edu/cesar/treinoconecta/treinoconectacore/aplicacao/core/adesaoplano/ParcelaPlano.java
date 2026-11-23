package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.MensalidadeIdentificador;

/**
 * Uma cobrança do cronograma da adesão. O valor é o devido na data para parcelas em aberto ou em atraso, o pago para
 * parcelas pagas e o valor atual do plano para parcelas previstas.
 */
public record ParcelaPlano(int numero, Optional<MensalidadeIdentificador> mensalidade, LocalDate periodoInicio,
        LocalDate periodoFim, LocalDate vencimento, BigDecimal valor, SituacaoParcela situacao) { }
