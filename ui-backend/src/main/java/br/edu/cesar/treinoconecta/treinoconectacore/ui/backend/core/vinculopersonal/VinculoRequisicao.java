package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.vinculopersonal;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;

public record VinculoRequisicao(ProfissionalIdentificador profissional, AlunoIdentificador aluno) { }
