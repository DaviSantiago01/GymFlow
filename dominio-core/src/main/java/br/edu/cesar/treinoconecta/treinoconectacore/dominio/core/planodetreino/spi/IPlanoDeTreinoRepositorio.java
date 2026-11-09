package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IPlanoDeTreinoRepositorio extends IAgregadoRepositorio<PlanoDeTreinoIdentificador, PlanoDeTreino> {
    PlanoDeTreino bloquear(PlanoDeTreinoIdentificador identificador);
    boolean existeAtivoPorAluno(AlunoIdentificador aluno);
    PlanoDeTreino buscarAtivoPorAluno(AlunoIdentificador aluno);
}
