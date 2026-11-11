package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.Aluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public record AlunoProjecao(AlunoIdentificador identificador, String usuario, String nome) implements IProjecao<Aluno> {
    public AlunoProjecao(Long identificador, String usuario, String nome) {
        this(new AlunoIdentificador(identificador.toString()), usuario, nome);
    }
}
