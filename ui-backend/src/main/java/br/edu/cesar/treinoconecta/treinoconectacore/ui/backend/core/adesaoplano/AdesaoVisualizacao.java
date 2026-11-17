package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.adesaoplano;

import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano.AdesaoPlanoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.PlanoProjecao;

/** A adesão com suas mensalidades e os planos disponíveis para uma troca. */
public record AdesaoVisualizacao(AdesaoPlanoProjecao adesao, List<MensalidadeProjecao> mensalidades,
        List<PlanoProjecao> planos) { }
