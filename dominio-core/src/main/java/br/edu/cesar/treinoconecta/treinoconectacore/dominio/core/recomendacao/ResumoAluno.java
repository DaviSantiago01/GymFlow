package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao;

import java.util.List;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;

/** Situação da matrícula e plano de treino ativo do aluno, exibidos junto do perfil e das recomendações. */
public record ResumoAluno(String aluno, String nome, boolean matriculaAtiva, Optional<PlanoTreinoResumido> plano) {
    public record PlanoTreinoResumido(String identificador, String nome, int versao, String personal, String personalNome,
            int frequenciaSemanal, List<DiaSemana> dias) {
    }
}
