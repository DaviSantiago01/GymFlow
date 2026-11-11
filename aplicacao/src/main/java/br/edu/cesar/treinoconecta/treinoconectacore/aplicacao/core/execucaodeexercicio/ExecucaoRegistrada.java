package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio;

import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.SugestaoProgressao;

public record ExecucaoRegistrada(ExecucaoDeExercicioProjecao execucao, Optional<SugestaoProgressao> sugestao) { }
