package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino;

import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.ExercicioProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;

public record PlanoDeTreinoFormulario(List<AlunoProjecao> alunos, List<ExercicioProjecao> exercicios,
        List<FaixaObjetivo> objetivos, List<DiaSemana> dias, int intervaloMinimoSegundos, int intervaloMaximoSegundos) { }
