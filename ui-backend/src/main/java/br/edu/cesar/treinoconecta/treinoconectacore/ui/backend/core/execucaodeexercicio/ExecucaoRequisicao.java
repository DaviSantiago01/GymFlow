package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.execucaodeexercicio;

import java.math.BigDecimal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;

public record ExecucaoRequisicao(PlanoDeTreinoIdentificador plano, DiaSemana dia, ExercicioIdentificador exercicio,
        int series, int repeticoes, BigDecimal carga) { }
