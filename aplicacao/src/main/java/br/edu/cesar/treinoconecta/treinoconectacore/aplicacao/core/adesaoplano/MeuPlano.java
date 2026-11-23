package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeProjecao;

/**
 * Consulta do próprio aluno: a adesão ativa, quando houver, o cronograma de parcelas dessa adesão e todas as suas
 * mensalidades, inclusive de adesões anteriores.
 */
public record MeuPlano(Optional<AdesaoPlanoProjecao> adesao, List<MensalidadeProjecao> mensalidades, LocalDate hoje,
        Optional<CronogramaAdesao> cronograma) { }
