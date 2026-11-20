package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi;

import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Cpf;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IAlunoRepositorio extends IAgregadoRepositorio<AlunoIdentificador, Aluno> {
    boolean existePorUsuario(String usuario);
    Aluno buscarPorUsuario(String usuario);
    Aluno bloquearPorUsuario(String usuario);
    Aluno bloquear(AlunoIdentificador identificador);
    boolean existePorCpf(Cpf cpf, Optional<AlunoIdentificador> identificadorIgnorado);
    boolean existePorEmail(Email email, Optional<AlunoIdentificador> identificadorIgnorado);
    /** Remove o aluno, a credencial e o histórico; só é chamado quando não há outros registros do aluno. */
    void excluirDefinitivamente(AlunoIdentificador identificador);
}
