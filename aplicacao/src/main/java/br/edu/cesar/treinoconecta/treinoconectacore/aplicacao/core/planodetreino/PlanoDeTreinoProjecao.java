package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino;

import java.time.Instant;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.SituacaoPlano;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public record PlanoDeTreinoProjecao(PlanoDeTreinoIdentificador identificador, AlunoIdentificador aluno, String alunoNome,
        PersonalIdentificador personal, String personalNome, String nome, int frequenciaSemanal, SituacaoPlano situacao,
        int versao, Instant criacao, Optional<Instant> encerramento) implements IProjecao<PlanoDeTreino> {
    public PlanoDeTreinoProjecao(Long identificador, Long aluno, String alunoNome, Long personal, String personalNome,
            String nome, int frequenciaSemanal, SituacaoPlano situacao, int versao, Instant criacao,
            @Nullable Instant encerramento) {
        this(new PlanoDeTreinoIdentificador(identificador.toString()), new AlunoIdentificador(aluno.toString()), alunoNome,
                new PersonalIdentificador(personal.toString()), personalNome, nome, frequenciaSemanal, situacao, versao,
                criacao, Optional.ofNullable(encerramento));
    }
}
