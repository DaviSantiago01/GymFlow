package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.professor;

import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor.*;
public record PerfilProfessor(ProfessorProjecao professor,List<AvaliacaoProfessorProjecao> recentes) { }

