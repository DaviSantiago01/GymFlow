package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.spi;

import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.academia.AcademiaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.RegistroAcesso;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.RegistroAcessoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IRegistroAcessoRepositorio extends IAgregadoRepositorio<RegistroAcessoIdentificador, RegistroAcesso> {
    /** Entrada ainda sem saída do aluno, bloqueada para a atualização. */
    Optional<RegistroAcesso> bloquearAbertoDoAluno(AlunoIdentificador aluno);

    /**
     * Bloqueia a academia e conta os alunos presentes, para que entradas simultâneas de alunos diferentes não
     * ultrapassem a capacidade máxima (F006, RN010).
     */
    long contarPresentesComBloqueio(AcademiaIdentificador academia);
}
