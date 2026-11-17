package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeProjecao;

/** Consulta do próprio aluno: a adesão ativa, quando houver, e todas as suas mensalidades. */
public record MeuPlano(Optional<AdesaoPlanoProjecao> adesao, List<MensalidadeProjecao> mensalidades, LocalDate hoje) { }
