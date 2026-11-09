package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino;

import java.math.BigDecimal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;

public record SerieDetalhe(ExercicioIdentificador exercicio, String exercicioNome, int quantidade, int repeticoesMinimas,
        int repeticoesMaximas, BigDecimal carga, int intervaloSegundos) { }
