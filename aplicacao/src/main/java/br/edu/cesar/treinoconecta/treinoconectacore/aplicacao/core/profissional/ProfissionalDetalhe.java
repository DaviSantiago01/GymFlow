package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.profissional;

import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.vinculopersonal.VinculoPersonalAlunoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Profissional;

/** Tela de gerenciamento: dados, especialidades, jornada semanal e vínculos do profissional. */
public record ProfissionalDetalhe(Profissional profissional, String cpfFormatado, long cargaHorariaSemanalMinutos,
        long limiteSemanalMinutos, List<VinculoPersonalAlunoProjecao> vinculos, List<AlunoProjecao> alunos) { }
