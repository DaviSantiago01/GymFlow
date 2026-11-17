package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor;

import java.util.Optional;
public record MinhaAvaliacao(Optional<AvaliacaoProfessorProjecao> avaliacao,boolean jaAvaliou) { }

