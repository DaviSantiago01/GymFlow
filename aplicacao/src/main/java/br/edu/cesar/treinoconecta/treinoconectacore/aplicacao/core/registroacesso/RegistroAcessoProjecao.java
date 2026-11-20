package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso;

import java.time.Instant;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.RegistroAcesso;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.RegistroAcessoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public record RegistroAcessoProjecao(RegistroAcessoIdentificador identificador, String aluno, String alunoNome,
        String alunoUsuario, Instant entrada, @Nullable Instant saida, boolean verificar) implements IProjecao<RegistroAcesso> {
    public RegistroAcessoProjecao(Long identificador, Long aluno, String alunoNome, String alunoUsuario, Instant entrada,
            @Nullable Instant saida, Boolean verificar) {
        this(new RegistroAcessoIdentificador(identificador.toString()), aluno.toString(), alunoNome, alunoUsuario, entrada,
                saida, verificar);
    }
}
