package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno;

import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.SituacaoAluno;

/** Linha da lista de alunos da administração (F005), com a situação calculada pela RN009. */
public record AlunoGestaoProjecao(String identificador, String usuario, String nome, String cpf, String email,
        String telefone, @Nullable String plano, SituacaoAluno situacao) {
    public AlunoGestaoProjecao(Long identificador, String usuario, String nome, @Nullable String cpf,
            @Nullable String email, @Nullable String telefone, Boolean ativo, @Nullable String motivoBloqueio,
            @Nullable String plano, Boolean mensalidadeAtrasada) {
        this(identificador.toString(), usuario, nome, cpf == null ? "" : cpf, email == null ? "" : email,
                telefone == null ? "" : telefone, plano,
                SituacaoAluno.calcular(ativo, motivoBloqueio != null, plano != null, mensalidadeAtrasada));
    }
}
