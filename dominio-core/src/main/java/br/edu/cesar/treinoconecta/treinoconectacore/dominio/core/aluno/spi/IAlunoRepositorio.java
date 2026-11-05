package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.*;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;
public interface IAlunoRepositorio extends IAgregadoRepositorio<AlunoIdentificador, Aluno> {
    boolean existePorUsuario(String usuario);
    Aluno buscarPorUsuario(String usuario);
    Aluno bloquearPorUsuario(String usuario);
}
