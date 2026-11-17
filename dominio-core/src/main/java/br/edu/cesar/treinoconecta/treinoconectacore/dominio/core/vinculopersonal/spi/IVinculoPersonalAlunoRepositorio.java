package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAlunoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IVinculoPersonalAlunoRepositorio
        extends IAgregadoRepositorio<VinculoPersonalAlunoIdentificador, VinculoPersonalAluno> {
    VinculoPersonalAluno bloquear(VinculoPersonalAlunoIdentificador identificador);
    boolean existeAtivo(ProfissionalIdentificador profissional, AlunoIdentificador aluno);
    boolean existeAtivoDoProfissional(ProfissionalIdentificador profissional);
}
