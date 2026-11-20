package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso.FrequenciaAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.SituacaoAluno;

/** Tela de detalhes do aluno na administração: dados, situação, matrícula, histórico e frequência recente. */
public record AlunoFicha(String identificador, String usuario, String nome, @Nullable String cpf,
        @Nullable LocalDate nascimento, @Nullable Integer idade, boolean menorDeIdade, @Nullable String email,
        String telefone, String endereco, @Nullable Responsavel responsavel, boolean ativo,
        @Nullable String motivoBloqueio, @Nullable Instant inicioBloqueio, boolean cadastroCompleto,
        SituacaoAluno situacao, boolean mensalidadeAtrasada, @Nullable MatriculaResumo matricula, boolean presente,
        List<HistoricoAlunoProjecao> historico, FrequenciaAluno frequenciaRecente) {

    public record Responsavel(String nome, String cpf, String telefone) { }
}
