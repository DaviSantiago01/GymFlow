package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.SugestaoProgressao;

public record EvolucaoExercicio(AlunoIdentificador aluno, Optional<ExercicioIdentificador> exercicio,
        List<ExercicioExecutado> exercicios, List<ExecucaoDeExercicioProjecao> execucoes, Optional<BigDecimal> maiorCarga,
        Optional<SugestaoProgressao> sugestao) { }
