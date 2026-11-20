package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso.spi;

import java.time.Instant;
import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso.RegistroAcessoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso.RegistroAcessoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IRegistroAcessoAplicacaoRepositorio
        extends IAgregadoAplicacaoRepositorio<RegistroAcessoProjecao, RegistroAcessoConsulta> {
    /** Entradas sem saída registrada, da mais antiga para a mais recente (F006, RN010). */
    List<RegistroAcessoProjecao> presentes();

    /** Entradas do aluno no intervalo [inicio, fim), da mais recente para a mais antiga (RN011 e RN012). */
    List<RegistroAcessoProjecao> doAluno(AlunoIdentificador aluno, Instant inicio, Instant fim);
}
